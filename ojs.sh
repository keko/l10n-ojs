#!/bin/bash

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>

# © Enrique Estévez Fernández <keko.gl[fix@]gmail[fix.]com>
# © Proxecto Trasno <proxecto[fix@]trasno[fix.]net>
# april 2013

## -

# Código e estrutura do script baseado noutro de Miguel Bouzada

# execute « [bash |./]ojs [getXml|xml2po|po2xml] »

## TODO: Partese de que se traballa coa versión de código ojs-2.4.2 e que
## está descargada e descomprimida no mesmo cartafol onde está este script
## Hai que realizar todas as comprobacións en cada unha das funcións do script
## para controlar erros, poder pasar a versión do ojs e o idioma no que se
## traducirá como un argumento e ata a posibilidade de que a descargue desde
## Internet e a descomprima. Tamén se pode comprobar que non está instalada.
## a ferramenta po4a (o conversor actual de xml a po e viceversa) e que a instale.
## Nalgún momento investigar outro conversor, xa que este non resolve 100%.
## Probar con: https://github.com/mate-desktop/mate-doc-utils/tree/master/xml2po
## Outra opción sería programar algo en Java, e facer un conversor de xml a xliff.


function getXml(){
	## Esta función xera os cartafoles (coa estrutura de ojs):
	## en_US: cos ficheiros xml que hai que traducir
	## gl: sen ficheiros, conterá os ficheiros convertidos unha vez traducidos
	## po: sen ficheiros, conterá os ficheiros que hai que traducir convertidos a po
	## xm-gl: cos ficheiros xml que xa hai traducidos deste idioma
    cd ojs-2.4.2
    find . -name "en_US" > ../ficheiros.txt
    cd ..
    while read linea
    do
        mkdir -p ./en_US/$linea
        mkdir -p ./gl/$linea
		mkdir -p ./po/$linea
		mkdir -p ./xml-gl/${linea/en_US/gl_ES}
        cp -r ./ojs-2.4.2/$linea/* ./en_US/$linea/
		cp -r ./ojs-2.4.2/${linea/en_US/gl_ES}/* ./xml-gl/${linea/en_US/gl_ES}/
    done < ficheiros.txt
    rm ficheiros.txt
}

function getTmx(){
	## Esta función xera a tmx co traballo existente no locale que se quere traducir.
	## Saca un listado dos ficheiros xml no locale en_US e supón que existen no locale a traducir.
	## A continuación chama a un aplicativo externo programado en Java que xera a tmx.
	find en_US -name "*.xml" > ficheiros_xml_eng.txt
	sed -e 's/en_US/xml-gl/' ficheiros_xml_eng.txt > temporal.txt
	sed -e 's/en_US/gl_ES/' temporal.txt > ficheiros_xml_gal.txt
    rm temporal.txt
	java XerarTMX ficheiros_xml_eng.txt ficheiros_xml_gal.txt
    rm ficheiros_xml_eng.txt
	rm ficheiros_xml_gal.txt
}

function xmlTOpo(){
	## Esta función fai conversións masivas de XML a PO
	## Os ficheiros convertidos terán o mesmo nome engadindo a extensión .po
	## Son copiados dentro do cartafol po na mesma estrutura que tiñan no cartafol en_US
	## A conversión realizase coa ferramenta po4a
    cd en_US
    find . -name "*.xml" > ../ficheiros_xml.txt
    cd ..
    while read entrada
    do
        po4a-gettextize -f xml -m ./en_US/$entrada -p ./po/$entrada.po
    done < ficheiros_xml.txt
    rm ficheiros_xml.txt
}

function poTOxml(){
	## Esta función fai conversións masivas de XML a PO
	## Son copiados dentro do cartafol gl na mesma estrutura que tiñan no cartafol en_US
	## A conversión realizase coa ferramenta po4a
    cd en_US
    find . -name "*.xml" > ../ficheiros_xml.txt
    cd ..
    while read entrada
    do
        po4a-translate -f xml -m ./en_US/$entrada -p ./po/$entrada.po -l ./gl/$entrada -k 0
    done < ficheiros_xml.txt
    rm ficheiros_xml.txt
}


param=$1
[ $param = "" ] && exit 0
[ $param = getXml ] && getXml
[ $param = getTmx ] && getTmx
[ $param = xml2po ] && xmlTOpo
[ $param = po2xml ] && poTOxml

#.EOF
