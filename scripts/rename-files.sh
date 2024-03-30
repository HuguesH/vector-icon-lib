for file in ./../OfficeSymbols_2014/**/*';_'*svg
do
  if [ -e "${file//;_/_}" ]
  then
    printf >&2 '%s\n' "Warning, skipping $file as the renamed version already exists"
    continue
  fi
  printf  "Change space for $file \n"
  mv -- "$file" "${file//;_/_}"
done