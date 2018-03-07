cp jeval ../release/jeval
cp jeval.bat ../release/jeval
cd ../release
rm jeval.zip
zip -9r jeval.zip jeval 
#rm -rf jeval/*
