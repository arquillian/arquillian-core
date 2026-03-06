#!/usr/bin/env bash

#
# Copyright The Arquillian Authors
# SPDX-License-Identifier: Apache-2.0
#

# Strict mode: fail on error, fail on unset vars, fail on pipe failure
set -o errexit
set -o nounset
set -o pipefail

# Formatting functions

fail() {
    printf "%s%s%s\n\n" "${RED}" "${1}" "${CLEAR}"
    printHelp
    exit 1
}

failNoHelp() {
    printf "%s%s%s\n" "${RED}" "${1}" "${CLEAR}"
    exit 1
}

printArgHelp() {
    if [ -z "${1}" ]; then
        printf "${YELLOW}    %-20s${CLEAR}%s\n" "${2}" "${3}"
    else
        printf "${YELLOW}%s, %-20s${CLEAR}%s\n" "${1}" "${2}" "${3}"
    fi
}

printHelp() {
    echo "Performs a release of the project. The release argument and value and the development argument and value are required parameters."
    echo "Any addition arguments are passed to the Maven command."
    echo ""
    printArgHelp "-d" "--development" "The next version for the development cycle."
    printArgHelp "-f" "--force" "Forces to allow a SNAPSHOT suffix in release version and not require one for the development version."
    printArgHelp "-h" "--help" "Displays this help."
    printArgHelp "" "--notes-start-tag" "When doing a GitHub release, indicates the tag to use as the starting point for generating release notes."
    printArgHelp "-p" "--prerelease" "Indicates this is a prerelease and the GitHub release should be marked as such."
    printArgHelp "-r" "--release" "The version to be released. Also used for the tag."
    printArgHelp "" "--dry-run" "Executes the release as a dry-run. Nothing will be updated or pushed."
    printArgHelp "-v" "--verbose" "Prints verbose output."
    echo ""
    echo "Usage: ${0##*/} --release 1.10.1.Final --development 1.10.2.Final-SNAPSHOT"
}

CLEAR=""
RED=""
YELLOW=""

# check if stdout is a terminal and NO_COLOR is not set
if [[ -t 1 ]] && [[ -z "${NO_COLOR-}" ]]; then
    if command -v tput >/dev/null 2>&1; then
        CLEAR=$(tput sgr0)
        RED=$(tput setaf 1)
        YELLOW=$(tput setaf 3)
    fi
fi

DRY_RUN=false
FORCE=false
DEVEL_VERSION=""
RELEASE_VERSION=""
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MVN="${SCRIPT_DIR}/mvnw"
LOCAL_REPO="/tmp/m2/repository/$(basename "${SCRIPT_DIR}")"
VERBOSE=false
GH_RELEASE_TYPE="--latest"
START_TAG=()
MAVEN_ARGS=()
DAYS="${DAYS:-5}" # Default to 5 if not set

# Parse arguments
while [ "$#" -gt 0 ]; do
    case "${1}" in
        -d|--development)
            DEVEL_VERSION="${2}"
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            ;;
        -f|--force)
            FORCE=true;
            ;;
        -h|--help)
            printHelp
            exit 0
            ;;
        --notes-start-tag)
            START_TAG=("--notes-from-tag" "${2}")
            shift
            ;;
        -p|--prerelease)
            GH_RELEASE_TYPE="--prerelease"
            ;;
        -r|--release)
            RELEASE_VERSION="${2}"
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            ;;
        *)
            MAVEN_ARGS+=("${1}")
            ;;
    esac
    shift
done

# Validation
if [ -z "${DEVEL_VERSION}" ]; then
    fail "The development version is required."
fi

if [ -z "${RELEASE_VERSION}" ]; then
    fail "The release version is required."
fi

if [ "${FORCE}" == "false" ]; then
    # Native bash string matching instead of grep
    if [[ "${RELEASE_VERSION}" == *"SNAPSHOT"* ]]; then
        failNoHelp "The release version appears to be a SNAPSHOT (${RELEASE_VERSION}). This is likely invalid. Use -f to force."
    fi
    if [[ "${DEVEL_VERSION}" != *"SNAPSHOT"* ]]; then
        failNoHelp "The development version does not appear to be a SNAPSHOT (${DEVEL_VERSION}). This is likely invalid. Use -f to force."
    fi
fi

# Find the expected Server ID
# We temporarily disable set -e here because mvn might fail if args are bad, and we want to capture that
set +e
SERVER_ID=$("${MVN}" help:evaluate -Dexpression=central.serverId -q -DforceStdout "${MAVEN_ARGS[@]}" | sed 's/^\[INFO\] \[stdout\] //')
RET_CODE=$?
set -e

if [ $RET_CODE -ne 0 ]; then
    failNoHelp "Failed to evaluate Maven expression. Please check your Maven arguments."
fi

# Check the settings to ensure a server defined with that value
if ! "${MVN}" help:effective-settings | grep -q "<id>${SERVER_ID}</id>"; then
    failNoHelp "A server with the id of \"${SERVER_ID}\" was not found in your settings.xml file."
fi

printf "Performing release for version %s with the next version of %s\n" "${RELEASE_VERSION}" "${DEVEL_VERSION}"

TAG_NAME="${RELEASE_VERSION}"
MVN_FLAGS=()

if ${DRY_RUN}; then
    echo "This will be a dry run and nothing will be updated or pushed."
    MVN_FLAGS+=("-DdryRun" "-DpushChanges=false")
fi

# Clean up local repo
if [ -d "${LOCAL_REPO}" ]; then
    # Verbose flag logic for rm
    RM_FLAGS="-rf"
    if ${VERBOSE}; then RM_FLAGS="-rfv"; fi

    # Delete any directories over a day old
    find "${LOCAL_REPO}" -type d -mtime +"${DAYS}" -print0 | xargs -0 -I {} rm "${RM_FLAGS}" "{}"
    # Delete any SNAPSHOTs
    find "${LOCAL_REPO}" -type d -name "*SNAPSHOT" -print0 | xargs -0 -I {} rm "${RM_FLAGS}" "{}"

    # Delete directories associated with this project
    PROJECT_PATH="$("${MVN}" help:evaluate -Dexpression=project.groupId -q -DforceStdout "${MAVEN_ARGS[@]}")"
    # Safe replacement of dots with slashes
    PROJECT_PATH="${LOCAL_REPO}/${PROJECT_PATH//./\/}"

    if [ -d "${PROJECT_PATH}" ]; then
        rm "${RM_FLAGS}" "${PROJECT_PATH}"
    fi
fi

# Create the command
CMD=("${MVN}" clean release:clean release:prepare release:perform)
CMD+=("-Dmaven.repo.local=${LOCAL_REPO}")
CMD+=("-DdevelopmentVersion=${DEVEL_VERSION}")
CMD+=("-DreleaseVersion=${RELEASE_VERSION}")
CMD+=("-Dtag=${TAG_NAME}")

# Append extra maven flags calculated earlier
if [ ${#MVN_FLAGS[@]} -gt 0 ]; then
    CMD+=("${MVN_FLAGS[@]}")
fi

# Append any pass-through arguments
if [ ${#MAVEN_ARGS[@]} -gt 0 ]; then
    CMD+=("${MAVEN_ARGS[@]}")
fi

if ${VERBOSE}; then
    printf "\n\nExecuting:\n  %s\n" "${CMD[*]}"
fi

# Execute the command
# "${CMD[@]}" expands the array respecting spaces within arguments
"${CMD[@]}"

# GitHub Release Handling

if command -v gh &>/dev/null; then
    # Check for default repo quietly
    if ! gh repo set-default --view &>/dev/null; then
        echo ""
        echo -e "${RED}No default repository has been set. You must use 'gh repo set-default' to set a default repository before executing the following commands.${CLEAR}"
        echo ""
        echo "gh release create --generate-notes ${START_TAG[*]} ${GH_RELEASE_TYPE} --verify-tag ${TAG_NAME}"
    else
        if ${DRY_RUN}; then
            printf "${YELLOW}Dry run would execute:${CLEAR}\ngh release create --generate-notes %s %s --verify-tag %s\n" "${START_TAG[*]}" "${GH_RELEASE_TYPE}" "${TAG_NAME}"
        else
            gh release create --generate-notes "${START_TAG[@]}" "${GH_RELEASE_TYPE}" --verify-tag "${TAG_NAME}"
        fi
    fi
else
    echo ""
    echo "The gh commands are not available. You must manually create a release for the GitHub tag ${TAG_NAME}."
fi
