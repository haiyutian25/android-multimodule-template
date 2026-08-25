#!/bin/bash

# Verify bash version. macOS comes with bash 3 preinstalled.
if [[ ${BASH_VERSINFO[0]} -lt 4 ]]
then
  echo "You need at least bash 4 to run this script."
  exit 1
fi

# exit when any command fails
set -e

usage() {
  cat <<'EOF'
Usage: bash customizer.sh my.new.package MyNewDataModel [ApplicationName] [options]

Positional arguments:
  my.new.package      application id / base package (lowercase, dot separated)
  MyNewDataModel      data model name (PascalCase)
  ApplicationName     optional application class name (PascalCase, default: MyApplication)

Optional code-level customizations:
  --entity-field <name>     rename the entity field (default: name), e.g. title
  --fake-data <a,b,c>       fake data items used by tests (default: One,Two,Three)
  --database-class <Name>   rename the Room database class (default: AppDatabase)
  --main-activity <Name>    rename the main activity class (default: MainActivity)
  --query-method <name>     rename the DAO query method (default: getMyModels)
  --insert-method <name>    rename the DAO insert method (default: insertMyModel)
  --add-method <name>       rename the ViewModel add method (default: addMyModel)

Example:
  bash customizer.sh com.example.todo TodoItem TodoApp \
      --entity-field title --database-class TodoDatabase --main-activity TodoActivity \
      --query-method loadTodos --insert-method saveTodo --add-method addTodo \
      --fake-data "Buy milk,Walk dog"
EOF
}

# ---------- Parse arguments (positional + optional flags) ----------
POSITIONAL=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --entity-field|--fake-data|--database-class|--main-activity|--query-method|--insert-method|--add-method)
      if [[ -z ${2-} ]]; then
        echo "Option $1 requires a value." >&2
        exit 2
      fi
      case "$1" in
        --entity-field)   ENTITY_FIELD=$2 ;;
        --fake-data)      FAKE_DATA=$2 ;;
        --database-class) DATABASE_CLASS=$2 ;;
        --main-activity)  MAIN_ACTIVITY=$2 ;;
        --query-method)   QUERY_METHOD=$2 ;;
        --insert-method)  INSERT_METHOD=$2 ;;
        --add-method)     ADD_METHOD=$2 ;;
      esac
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    -*)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
    *)
      POSITIONAL+=("$1")
      shift
      ;;
  esac
done

if [[ ${#POSITIONAL[@]} -lt 2 ]]; then
   usage >&2
   exit 2
fi

PACKAGE=${POSITIONAL[0]}
DATAMODEL=${POSITIONAL[1]}
APPNAME=${POSITIONAL[2]:-MyApplication}
SUBDIR=${PACKAGE//.//} # Replaces . with /

# ---------- Validate inputs ----------
if [[ ! $PACKAGE =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
  echo "Package '$PACKAGE' must be lowercase dot-separated (e.g. com.example.app)." >&2
  exit 2
fi
if [[ ! $DATAMODEL =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
  echo "Data model '$DATAMODEL' must be PascalCase (e.g. TodoItem)." >&2
  exit 2
fi
if [[ ! $APPNAME =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
  echo "Application name '$APPNAME' must be PascalCase (e.g. TodoApp)." >&2
  exit 2
fi
if [[ -n ${ENTITY_FIELD-} && ! $ENTITY_FIELD =~ ^[a-z][A-Za-z0-9]*$ ]]; then
  echo "--entity-field '$ENTITY_FIELD' must be a camelCase identifier." >&2
  exit 2
fi
if [[ -n ${DATABASE_CLASS-} && ! $DATABASE_CLASS =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
  echo "--database-class '$DATABASE_CLASS' must be PascalCase." >&2
  exit 2
fi
if [[ -n ${MAIN_ACTIVITY-} && ! $MAIN_ACTIVITY =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
  echo "--main-activity '$MAIN_ACTIVITY' must be PascalCase." >&2
  exit 2
fi
for _opt in QUERY_METHOD INSERT_METHOD ADD_METHOD; do
  _val=${!_opt-}
  if [[ -n $_val && ! $_val =~ ^[a-z][A-Za-z0-9]*$ ]]; then
    echo "--$(echo "$_opt" | tr '[:upper:]' '[:lower:]' | sed 's/_/-/g') '$_val' must be a camelCase identifier." >&2
    exit 2
  fi
done

for n in $(find . -type d \( -path '*/src/androidTest' -or -path '*/src/main' -or -path '*/src/test' \) )
do
  # Skip source sets without template sources (e.g. the build-logic convention plugins)
  if [[ -d $n/java/android/template ]]; then
    echo "Creating $n/java/$SUBDIR"
    mkdir -p $n/java/$SUBDIR
    echo "Moving files to $n/java/$SUBDIR"
    mv $n/java/android/template/* $n/java/$SUBDIR
    echo "Removing old $n/java/android/template"
    rm -rf $n/java/android
  fi
done

# Rename package and imports
echo "Renaming packages to $PACKAGE"
find ./ -type f -name "*.kt" -exec sed -i.bak "s/package android.template/package $PACKAGE/g" {} \;
find ./ -type f -name "*.kt" -exec sed -i.bak "s/import android.template/import $PACKAGE/g" {} \;

# Gradle files
find ./ -type f -name "*.kts" -exec sed -i.bak "s/android.template/$PACKAGE/g" {} \;

# Rename model
echo "Renaming model to $DATAMODEL"
find ./ -type f -name "*.kt" -exec sed -i.bak "s/MyModel/${DATAMODEL^}/g" {} \; # First upper case
find ./ -type f -name "*.kt" -exec sed -i.bak "s/myModel/${DATAMODEL,}/g" {} \; # First lower case
find ./ -type f -name "*.kt*" -exec sed -i.bak "s/mymodel/${DATAMODEL,,}/g" {} \; # All lowercase

echo "Cleaning up"
find . -name "*.bak" -type f -delete

# Rename files
echo "Renaming files to $DATAMODEL"
find ./ -name "*MyModel*.kt" | sed "p;s/MyModel/${DATAMODEL^}/" | tr '\n' '\0' | xargs -0 -n 2 mv
# Rename feature module names (api/impl split)
if [[ -n $(find ./ -name "*-mymodel-api" -or -name "*-mymodel-impl") ]]
then
  echo "Renaming modules to $DATAMODEL"
  find ./ \( -name "*-mymodel-api" -or -name "*-mymodel-impl" \) -type d | sed "p;s/mymodel/${DATAMODEL,,}/" | tr '\n' '\0' | xargs -0 -n 2 mv
fi
# Rename directories
echo "Renaming directories to $DATAMODEL"
find ./ -name "mymodel" -type d  | sed "p;s/mymodel/${DATAMODEL,,}/" |  tr '\n' '\0' | xargs -0 -n 2 mv

# Rename app
if [[ $APPNAME != MyApplication ]]
then
    echo "Renaming app to $APPNAME"
    find ./ -type f \( -name "MyApplication.kt" -or -name "settings.gradle.kts" -or -name "*.xml" \) -exec sed -i.bak "s/MyApplication/$APPNAME/g" {} \;
    find ./ -name "MyApplication.kt" | sed "p;s/MyApplication/$APPNAME/" | tr '\n' '\0' | xargs -0 -n 2 mv
    find . -name "*.bak" -type f -delete
fi

# ---------- Optional code-level customizations ----------

# A. Rename the entity field (default: name)
if [[ -n ${ENTITY_FIELD-} ]]
then
    echo "Renaming entity field 'name' to '$ENTITY_FIELD'"
    find ./ -type f -name "*.kt" -exec sed -i.bak \
        -e "s/val name: String/val $ENTITY_FIELD: String/g" \
        -e "s/it\.name/it.$ENTITY_FIELD/g" \
        -e "s/model\.name/model.$ENTITY_FIELD/g" \
        -e "s/\.toModel()\.name/.toModel().$ENTITY_FIELD/g" \
        -e "s/$DATAMODEL(name = name, uid = uid)/$DATAMODEL($ENTITY_FIELD = $ENTITY_FIELD, uid = uid)/g" \
        -e "s/$DATAMODEL(name = /$DATAMODEL($ENTITY_FIELD = /g" \
        -e "s/${DATAMODEL}Entity(name = /${DATAMODEL}Entity($ENTITY_FIELD = /g" {} \;
fi

# A. Replace the fake data used by tests/previews
if [[ -n ${FAKE_DATA-} ]]
then
    echo "Setting fake data to: $FAKE_DATA"
    IFS=',' read -ra _items <<< "$FAKE_DATA"
    _list=""
    for _item in "${_items[@]}"; do
        # trim surrounding whitespace
        _item="${_item#"${_item%%[![:space:]]*}"}"
        _item="${_item%"${_item##*[![:space:]]}"}"
        [[ -n $_item ]] || continue
        if [[ $_item == *\"* ]]; then
            echo "--fake-data items must not contain double quotes." >&2
            exit 2
        fi
        _list+="\"$_item\", "
    done
    _list="${_list%, }"
    if [[ -z $_list ]]; then
        echo "--fake-data must contain at least one item." >&2
        exit 2
    fi
    # escape the replacement for sed (delimiter: |)
    _list_esc=$(printf '%s' "$_list" | sed -e 's/[&\\|]/\\&/g')
    find ./ -type f -name "*.kt" -exec sed -i.bak -E \
        "s|listOf\(\"One\", \"Two\", \"Three\"\)|listOf($_list_esc)|g" {} \;
fi

# B. Rename the Room database class (default: AppDatabase)
if [[ -n ${DATABASE_CLASS-} ]]
then
    echo "Renaming AppDatabase to $DATABASE_CLASS"
    find ./ -type f -name "*.kt" -exec sed -i.bak "s/AppDatabase/$DATABASE_CLASS/g" {} \;
    find ./ -type f -name "AppDatabase.kt" | while IFS= read -r _f; do
        mv "$_f" "$(dirname "$_f")/$DATABASE_CLASS.kt"
        echo "Renamed file: AppDatabase.kt -> $DATABASE_CLASS.kt"
    done
fi

# B. Rename the main activity class (default: MainActivity)
if [[ -n ${MAIN_ACTIVITY-} ]]
then
    echo "Renaming MainActivity to $MAIN_ACTIVITY"
    find ./ -type f \( -name "*.kt" -or -name "*.xml" \) -exec sed -i.bak "s/MainActivity/$MAIN_ACTIVITY/g" {} \;
    find ./ -type f -name "MainActivity.kt" | while IFS= read -r _f; do
        mv "$_f" "$(dirname "$_f")/$MAIN_ACTIVITY.kt"
        echo "Renamed file: MainActivity.kt -> $MAIN_ACTIVITY.kt"
    done
fi

# C. Rename the DAO query method (getMyModels after the model rename)
if [[ -n ${QUERY_METHOD-} ]]
then
    echo "Renaming DAO query method to $QUERY_METHOD"
    find ./ -type f -name "*.kt" -exec sed -i.bak "s/get${DATAMODEL}s/$QUERY_METHOD/g" {} \;
fi

# C. Rename the DAO insert method (insertMyModel after the model rename)
if [[ -n ${INSERT_METHOD-} ]]
then
    echo "Renaming DAO insert method to $INSERT_METHOD"
    find ./ -type f -name "*.kt" -exec sed -i.bak "s/insert${DATAMODEL}/$INSERT_METHOD/g" {} \;
fi

# C. Rename the ViewModel add method (addMyModel after the model rename)
if [[ -n ${ADD_METHOD-} ]]
then
    echo "Renaming ViewModel add method to $ADD_METHOD"
    find ./ -type f -name "*.kt" -exec sed -i.bak "s/add${DATAMODEL}/$ADD_METHOD/g" {} \;
fi

echo "Cleaning up"
find . -name "*.bak" -type f -delete

# Remove additional files
echo "Removing additional files"
rm -rf .google/
rm -rf .github/
rm -rf CONTRIBUTING.md LICENSE README.md
# The customizer script used to delete itself here; keep it so it can be re-run/inspected.
# rm -rf customizer.sh
rm -rf .git/
echo "Done!"