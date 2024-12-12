@echo off
echo Downloading
curl https://game.havenandhearth.com/res/%1 -o ./resources/custom/res/%1 --create-dirs -k