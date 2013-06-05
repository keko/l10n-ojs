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

# execute « [bash |./]ojs [getXml|getTmx|xml2po|po2xml] »

## TODO: Partese de que se traballa cunha versión de código de ojs e que
## está descargada e descomprimida no mesmo cartafol onde está este script
## Hai que realizar todas as comprobacións en cada unha das funcións do script
## para controlar erros, poder pasar a versión do ojs e ata a posibilidade de que a descargue desde
## Internet e a descomprima. Tamén se pode comprobar que non está instalada.
## a ferramenta po4a (o conversor actual de xml a po e viceversa) e que a instale.
## Nalgún momento investigar outro conversor, xa que este non resolve 100%.
## Probar con: https://github.com/mate-desktop/mate-doc-utils/tree/master/xml2po
## Outra opción sería programar algo en Java, e facer un conversor de xml a xliff.

Ruta_Root="/home/kike/code/ojs"
Source="en_US"
Target="gl_ES"
Version="ojs-2.4.2"


function getXml(){
	## Esta función xera os cartafoles (coa estrutura de ojs):
	## Source: cos ficheiros xml que hai que traducir
	## Target: sen ficheiros, conterá os ficheiros convertidos unha vez traducidos
	## po: sen ficheiros, conterá os ficheiros que hai que traducir convertidos a po
	## xml-Target: cos ficheiros xml que xa hai traducidos deste idioma
	cd ${Ruta_Root}/${Version}
    find . -name ${Source} > ../ficheiros.txt
    cd ..
    while read linea
    do
        mkdir -p ./${Source}/${linea}
        mkdir -p ./${Target}/${linea}
		mkdir -p ./po/${linea}
		mkdir -p ./xml-${Target}/${linea/$Source/$Target}
        cp -r ./${Version}/${linea}/* ./${Source}/$linea/
		[ -d ./${Version}/${linea/$Source/$Target} ] && cp -r ./${Version}/${linea/$Source/$Target}/* ./xml-${Target}/${linea/$Source/$Target}/
    done < ficheiros.txt
    rm ficheiros.txt
}


function getTmx(){
	## Esta función xera a tmx co traballo existente no locale que se quere traducir.
	## Saca un listado dos ficheiros xml no locale Source e supón que existen no locale a traducir.
	## A continuación chama a un aplicativo externo programado en Java que xera a tmx.
	find $Source -name "*.xml" > ficheiros-xml-source.txt
	sed -e s/$Source/xml-$Target/ ficheiros-xml-source.txt > temporal.txt
	sed -e s/$Source/$Target/ temporal.txt > ficheiros-xml-target.txt
    rm temporal.txt
	java XerarTMX ficheiros-xml-source.txt ficheiros-xml-target.txt $Source $Target
	rm ficheiros-xml-source.txt
	rm ficheiros-xml-target.txt
}


function xmlTOpo(){
	## Esta función fai conversións masivas de XML a PO
	## Os ficheiros convertidos terán o mesmo nome engadindo a extensión .po
	## Son copiados dentro do cartafol po na mesma estrutura que tiñan no cartafol Source
	## A conversión realizase coa ferramenta po4a
    cd $Source
    find . -name "*.xml" > ../ficheiros-xml.txt
    cd ..
    while read entrada
    do
        po4a-gettextize -f xml -m ./${Source}/$entrada -p ./po/$entrada.po
    done < ficheiros-xml.txt
    rm ficheiros-xml.txt
}


function poTOxml(){
	## Esta función fai conversións masivas de XML a PO
	## Son copiados dentro do cartafol Target na mesma estrutura que tiñan no cartafol Source
	## A conversión realizase coa ferramenta po4a
    cd $Source
    find . -name "*.xml" > ../ficheiros-xml.txt
    cd ..
    while read entrada
    do
        po4a-translate -f xml -m ./${Source}/$entrada -p ./po/$entrada.po -l ./${Target}/$entrada -k 0
    done < ficheiros-xml.txt
    rm ficheiros-xml.txt
}


param=$1
[ $param = "" ] && exit 0
[ $param = getXml ] && getXml
[ $param = getTmx ] && getTmx
[ $param = xml2po ] && xmlTOpo
[ $param = po2xml ] && poTOxml

#.EOF
