function exportEnhancedMetafile(){
  #inkscape --file <Input-file> --export-emf <output-file>
  svgFilePath=$1
  printf "exportEnhancedMetafile - read svgFilePath %svgFilePath  \n"
  emfFilePath=${svgFilePath//.svg/.emf}
  printf "exportEnhancedMetafile - export emfFilePath %emfFilePath \n"
  /bin/inkscape ${svgFilePath} --export-type='emf' ${emfFilePath}
}

for svgFilePath in ./../OfficeSymbols_2014/**/**.svg
do
  exportEnhancedMetafile "$svgFilePath"
done