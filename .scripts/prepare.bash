#!/dev/null

if ! test "${#}" -eq 0 ; then
	echo "[ee] invalid arguments; aborting!" >&2
	exit 1
fi

if test ! -e "${_outputs}" ; then
	if test -L "${_outputs}" ; then
		_outputs_store="$( readlink -- "${_outputs}" )"
	else
		_outputs_store="${_temporary}/$( basename -- "${_workbench}" )--$( readlink -m -- "${_outputs}" | tr -d '\n' | md5sum -t | tr -d ' \n-' )"
		ln -s -T -- "${_outputs_store}" "${_outputs}"
	fi
	if test ! -e "${_outputs_store}" ; then
		mkdir -- "${_outputs_store}"
	fi
fi

case "${_maven_pom_classifier}" in
	
	( component | *-component )
		env "${_mvn_env[@]}" "${_mvn_bin}" \
				-f "${_mvn_pom}" \
				--projects "${_maven_pom_group}:${_maven_pom_artifact}" \
				--also-make \
				--update-snapshots \
				--fail-never \
				"${_mvn_args[@]}" \
				dependency:go-offline \
				initialize
	;;
	
	( artifacts )
		# FIXME: We have to fix this...
		env "${_mvn_env[@]}" "${_mvn_bin}" \
				-f "${_mvn_pom}" \
				--also-make \
				--update-snapshots \
				--fail-never \
				"${_mvn_args[@]}" \
				dependency:go-offline \
				initialize
	;;
	
	( * )
		exit 1
	;;
esac

exit 0
