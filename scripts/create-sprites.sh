#for file in ./../optimized/OfficeSymbols_2014/**/*';_'*svg
for dir in ./../optimized/**/**
do
  if [ -d "${dir}" ] ;
  then
    puml="./../sprites/$(basename ${dir}).puml"
    touch  ${puml}

    printf "@startuml" >> ${puml}
    for svg in ${dir}/**svg
    do
      sprite="$"
      sprite+=$(basename ${svg} .svg)
      printf "\nsprite ${sprite} " >>  ${puml}
      cat ${svg} >> ${puml}
    done
    printf "\nlistsprites\n@enduml" >> ${puml}
  fi
done

