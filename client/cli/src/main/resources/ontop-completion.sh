#!/bin/bash

# Generated by airline BashCompletionGenerator

containsElement () {
  # This function from http://stackoverflow.com/a/8574392/107591
  local e
  for e in "${@:2}"; do [[ "$e" == "$1" ]] && return 0; done
  return 1
}

function _complete_ontop_command_version() {
  # Get completion data
  COMPREPLY=()
  CURR_WORD=${COMP_WORDS[COMP_CWORD]}
  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}
  COMMANDS=$1

  FLAG_OPTS=""
  ARG_OPTS=""

  ARGUMENTS=
  COMPREPLY=( $(compgen -W "${FLAG_OPTS} ${ARG_OPTS} ${ARGUMENTS}" -- ${CURR_WORD}) )
  echo ${COMPREPLY[@]}
  return 0
}

function _complete_ontop_command_help() {
  # Get completion data
  COMPREPLY=()
  CURR_WORD=${COMP_WORDS[COMP_CWORD]}
  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}
  COMMANDS=$1

  FLAG_OPTS=""
  ARG_OPTS=""

  ARGUMENTS=
  COMPREPLY=( $(compgen -W "${FLAG_OPTS} ${ARG_OPTS} ${ARGUMENTS}" -- ${CURR_WORD}) )
  echo ${COMPREPLY[@]}
  return 0
}

function _complete_ontop_command_query() {
  # Get completion data
  COMPREPLY=()
  CURR_WORD=${COMP_WORDS[COMP_CWORD]}
  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}
  COMMANDS=$1

  FLAG_OPTS="--disable-reasoning --enable-annotations"
  ARG_OPTS="-m -o -p -q --mapping -t --properties --query --ontology --output"

  $( containsElement ${PREV_WORD} ${ARG_OPTS[@]} )
  SAW_ARG=$?
  if [[ ${SAW_ARG} -eq 0 ]]; then
    ARG_VALUES=
    ARG_GENERATED_VALUES=
    case ${PREV_WORD} in
      -m|--mapping)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -t|--ontology)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -q|--query)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -o|--output)
        COMPREPLY=( $(compgen -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -p|--properties)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
    esac
  fi

  ARGUMENTS=
  COMPREPLY=( $(compgen -W "${FLAG_OPTS} ${ARG_OPTS} ${ARGUMENTS}" -- ${CURR_WORD}) )
  echo ${COMPREPLY[@]}
  return 0
}

function _complete_ontop_command_materialize() {
  # Get completion data
  COMPREPLY=()
  CURR_WORD=${COMP_WORDS[COMP_CWORD]}
  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}
  COMMANDS=$1

  FLAG_OPTS="--disable-reasoning --enable-annotations --no-streaming --separate-files"
  ARG_OPTS="-m -o -p --mapping -t --properties -f --format --ontology --output"

  $( containsElement ${PREV_WORD} ${ARG_OPTS[@]} )
  SAW_ARG=$?
  if [[ ${SAW_ARG} -eq 0 ]]; then
    ARG_VALUES=
    ARG_GENERATED_VALUES=
    case ${PREV_WORD} in
      -m|--mapping)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -t|--ontology)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -o|--output)
        COMPREPLY=( $(compgen -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -f|--format)
        COMPREPLY=( $(compgen -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -p|--properties)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
    esac
  fi

  ARGUMENTS=
  COMPREPLY=( $(compgen -W "${FLAG_OPTS} ${ARG_OPTS} ${ARGUMENTS}" -- ${CURR_WORD}) )
  echo ${COMPREPLY[@]}
  return 0
}

function _complete_ontop_command_bootstrap() {
  # Get completion data
  COMPREPLY=()
  CURR_WORD=${COMP_WORDS[COMP_CWORD]}
  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}
  COMMANDS=$1

  FLAG_OPTS=""
  ARG_OPTS="-m -p -b --mapping --base-iri -t --properties --ontology"

  $( containsElement ${PREV_WORD} ${ARG_OPTS[@]} )
  SAW_ARG=$?
  if [[ ${SAW_ARG} -eq 0 ]]; then
    ARG_VALUES=
    ARG_GENERATED_VALUES=
    case ${PREV_WORD} in
      -m|--mapping)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -t|--ontology)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -b|--base-iri)
        COMPREPLY=( $(compgen -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -p|--properties)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
    esac
  fi

  ARGUMENTS=
  COMPREPLY=( $(compgen -W "${FLAG_OPTS} ${ARG_OPTS} ${ARGUMENTS}" -- ${CURR_WORD}) )
  echo ${COMPREPLY[@]}
  return 0
}

function _complete_ontop_command_validate() {
  # Get completion data
  COMPREPLY=()
  CURR_WORD=${COMP_WORDS[COMP_CWORD]}
  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}
  COMMANDS=$1

  FLAG_OPTS=""
  ARG_OPTS="-m -p --mapping -t --properties --ontology"

  $( containsElement ${PREV_WORD} ${ARG_OPTS[@]} )
  SAW_ARG=$?
  if [[ ${SAW_ARG} -eq 0 ]]; then
    ARG_VALUES=
    ARG_GENERATED_VALUES=
    case ${PREV_WORD} in
      -m|--mapping)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -t|--ontology)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -p|--properties)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
    esac
  fi

  ARGUMENTS=
  COMPREPLY=( $(compgen -W "${FLAG_OPTS} ${ARG_OPTS} ${ARGUMENTS}" -- ${CURR_WORD}) )
  echo ${COMPREPLY[@]}
  return 0
}

function _complete_ontop_command_endpoint() {
  # Get completion data
  COMPREPLY=()
  CURR_WORD=${COMP_WORDS[COMP_CWORD]}
  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}
  COMMANDS=$1

  FLAG_OPTS="--lazy"
  ARG_OPTS="-m -p --mapping -t --properties --port --cors-allowed-origins --ontology"

  $( containsElement ${PREV_WORD} ${ARG_OPTS[@]} )
  SAW_ARG=$?
  if [[ ${SAW_ARG} -eq 0 ]]; then
    ARG_VALUES=
    ARG_GENERATED_VALUES=
    case ${PREV_WORD} in
      -m|--mapping)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -t|--ontology)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      --port)
        COMPREPLY=( $(compgen -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      --cors-allowed-origins)
        COMPREPLY=( $(compgen -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
      -p|--properties)
        COMPREPLY=( $(compgen -o default -W "${ARG_VALUES} ${ARG_GENERATED_VALUES}" -- ${CURR_WORD}) )
        echo ${COMPREPLY[@]}
        return 0
        ;;
    esac
  fi

  ARGUMENTS=
  COMPREPLY=( $(compgen -W "${FLAG_OPTS} ${ARG_OPTS} ${ARGUMENTS}" -- ${CURR_WORD}) )
  echo ${COMPREPLY[@]}
  return 0
}

function _complete_ontop() {
  # Get completion data
  CURR_WORD=${COMP_WORDS[COMP_CWORD]}
  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}
  CURR_CMD=
  if [[ ${COMP_CWORD} -ge 1 ]]; then
    CURR_CMD=${COMP_WORDS[1]}
  fi

  COMMANDS="help endpoint materialize query --version bootstrap validate"
  if [[ ${COMP_CWORD} -eq 1 ]]; then
    COMPREPLY=()
    COMPREPLY=( $(compgen -W "${COMMANDS}" -- ${CURR_WORD}) )
    return 0
  fi

  case ${CURR_CMD} in
    --version)
      COMPREPLY=( $(_complete_ontop_command_version "${COMMANDS}" ) )
      return $?
      ;;
    help)
      COMPREPLY=( $(_complete_ontop_command_help "${COMMANDS}" ) )
      return $?
      ;;
    query)
      COMPREPLY=( $(_complete_ontop_command_query "${COMMANDS}" ) )
      return $?
      ;;
    materialize)
      COMPREPLY=( $(_complete_ontop_command_materialize "${COMMANDS}" ) )
      return $?
      ;;
    bootstrap)
      COMPREPLY=( $(_complete_ontop_command_bootstrap "${COMMANDS}" ) )
      return $?
      ;;
    validate)
      COMPREPLY=( $(_complete_ontop_command_validate "${COMMANDS}" ) )
      return $?
      ;;
    endpoint)
      COMPREPLY=( $(_complete_ontop_command_endpoint "${COMMANDS}" ) )
      return $?
      ;;
  esac

}

complete -F _complete_ontop ontop
