git mv ./protobuf/core/pom.xml ./protobuf-core/pom.xml

git mv ./protobuf/globalproperties-plugin-translator/src/main/resources ./gcm-protobuf/globalproperties-plugin-translator/src/main/resources
git mv ./protobuf/groups-plugin-translator/src/main/resources ./gcm-protobuf/groups-plugin-translator/src/main/resources
git mv ./protobuf/materials-plugin-translator/src/main/resources ./gcm-protobuf/materials-plugin-translator/src/main/resources
git mv ./protobuf/nucleus-translator/src/main/resources ./gcm-protobuf/nucleus-translator/src/main/resources
git mv ./protobuf/people-plugin-translator/src/main/resources ./gcm-protobuf/people-plugin-translator/src/main/resources
git mv ./protobuf/personproperties-plugin-translator/src/main/resources ./gcm-protobuf/personproperties-plugin-translator/src/main/resources
git mv ./protobuf/properties-plugin-translator/src/main/resources ./gcm-protobuf/properties-plugin-translator/src/main/resources
git mv ./protobuf/plugin-translators-all/src/main/resources ./gcm-protobuf/plugin-translators-all/src/main/resources
git mv ./protobuf/proto-definitions/src/main/resources ./gcm-protobuf/proto-definitions/src/main/resources
git mv ./protobuf/regions-plugin-translator/src/main/resources ./gcm-protobuf/regions-plugin-translator/src/main/resources
git mv ./protobuf/reports-plugin-translator/src/main/resources ./gcm-protobuf/reports-plugin-translator/src/main/resources
git mv ./protobuf/resources-plugin-translator/src/main/resources ./gcm-protobuf/resources-plugin-translator/src/main/resources
git mv ./protobuf/stochastics-plugin-translator/src/main/resources ./gcm-protobuf/stochastics-plugin-translator/src/main/resources
git mv ./protobuf/globalproperties-plugin-translator/src/main/resources ./gcm-protobuf/globalproperties-plugin-translator/src/main/resources
git mv ./protobuf/globalproperties-plugin-translator/src/main/resources ./gcm-protobuf/globalproperties-plugin-translator/src/main/resources


rm  ./protobuf-core/pom.xml
rm  -r ./gcm-protobuf/globalproperties-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/groups-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/materials-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/nucleus-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/people-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/personproperties-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/properties-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/regions-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/reports-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/resources-plugin-translator/src/test/resources/json/*.json
rm  -r ./gcm-protobuf/stochastics-plugin-translator/src/test/resources/json/*.json

mv ./gcm-protobuf/globalproperties-plugin-translator/pomOld.xml ./gcm-protobuf/globalproperties-plugin-translator/pom.xml
mv ./gcm-protobuf/groups-plugin-translator/pomOld.xml ./gcm-protobuf/groups-plugin-translator/pom.xml
mv ./gcm-protobuf/materials-plugin-translator/pomOld.xml ./gcm-protobuf/materials-plugin-translator/pom.xml
mv ./gcm-protobuf/nucleus-translator/pomOld.xml ./gcm-protobuf/nucleus-translator/pom.xml
mv ./gcm-protobuf/people-plugin-translator/pomOld.xml ./gcm-protobuf/people-plugin-translator/pom.xml
mv ./gcm-protobuf/personproperties-plugin-translator/pomOld.xml ./gcm-protobuf/personproperties-plugin-translator/pom.xml
mv ./gcm-protobuf/properties-plugin-translator/pomOld.xml ./gcm-protobuf/properties-plugin-translator/pom.xml
mv ./gcm-protobuf/regions-plugin-translator/pomOld.xml ./gcm-protobuf/regions-plugin-translator/pom.xml
mv ./gcm-protobuf/reports-plugin-translator/pomOld.xml ./gcm-protobuf/reports-plugin-translator/pom.xml
mv ./gcm-protobuf/resources-plugin-translator/pomOld.xml ./gcm-protobuf/resources-plugin-translator/pom.xml
mv ./gcm-protobuf/stochastics-plugin-translator/pomOld.xml ./gcm-protobuf/stochastics-plugin-translator/pom.xml


git mv ./protobuf/globalproperties-plugin-translator/src/test ./gcm-protobuf/globalproperties-plugin-translator/src/test
git mv ./protobuf/groups-plugin-translator/src/test ./gcm-protobuf/groups-plugin-translator/src/test
git mv ./protobuf/materials-plugin-translator/src/test ./gcm-protobuf/materials-plugin-translator/src/test
git mv ./protobuf/nucleus-translator/src/test ./gcm-protobuf/nucleus-translator/src/test
git mv ./protobuf/people-plugin-translator/src/test ./gcm-protobuf/people-plugin-translator/src/test
git mv ./protobuf/personproperties-plugin-translator/src/test ./gcm-protobuf/personproperties-plugin-translator/src/test
git mv ./protobuf/properties-plugin-translator/src/test ./gcm-protobuf/properties-plugin-translator/src/test
git mv ./protobuf/regions-plugin-translator/src/test ./gcm-protobuf/regions-plugin-translator/src/test
git mv ./protobuf/reports-plugin-translator/src/test ./gcm-protobuf/reports-plugin-translator/src/test
git mv ./protobuf/resources-plugin-translator/src/test ./gcm-protobuf/resources-plugin-translator/src/test
git mv ./protobuf/stochastics-plugin-translator/src/test ./gcm-protobuf/stochastics-plugin-translator/src/test


git mv ./gcm-protobuf/plugin-translators-all/assembly.xml ./protobuf/plugin-translators-all/assembly.xml