import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/*
Agora o aplicativo lee dous ficheiros que se lle pasan como argumentos por consola. Estes ficheiros conteñen a lista de ficheiros que hai que emparellar. En ver de emparellar dous ficheiros, emparella un listado de ficheiros xml. Por agora só admite ficheiros xml que seguen a dtd locale do proxecto ojs e crea unha memoria de tradución en formato tmx.

O código dividiuse en dous métodos máis: emparellar_textos e escribir_tmx

Tívose que retirar a dtd locale da cabeceira dos ficheiros xml porque se producía un erro ao non atopala o parser.
<!DOCTYPE locale SYSTEM "../../lib/pkp/dtd/locale.dtd">

TODO
- Darlle soporte a máis dtd que se usan nos ficheiros de tradución do proxecto ojs.
- Explicar que se realiza nas diferentes partes do código.
- Conseguir que o proxecto traballe cos ficheiros xml sen ter que borrarlle a dtd nas cabeceiras.
- Mellorar a cabeceira do ficheiro tmx xerado para adaptala ao estándar.
- Pasar o idioma ao que se traduce por argumento, por agora só está pensado para o galego. Posiblemente o orixe, tamén se teña que pasar por argumento.

*/


public class XerarTMX {

	public static void main(String argv[]) {
		if (argv.length == 2) {
			File files_eng = new File(argv[0]);
			File files_gal = new File(argv[1]);
			if (files_eng.exists() && files_gal.exists()) {
				Document file_tmx;
				BufferedReader bfiles_eng, bfiles_gal;
				try {
					file_tmx = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					bfiles_eng = new BufferedReader(new FileReader(files_eng));
					bfiles_gal = new BufferedReader(new FileReader(files_gal));
					String sfile_eng, sfile_gal;
					while(bfiles_eng.ready() && bfiles_gal.ready()) {
						sfile_eng = bfiles_eng.readLine();
						sfile_gal = bfiles_gal.readLine();
						File ffile_eng, ffile_gal;
						ffile_eng = new File(sfile_eng);
						ffile_gal = new File(sfile_gal);
						if (ffile_eng.exists() && ffile_gal.exists()) {
							emparellar_textos(file_tmx,ffile_eng,ffile_gal);
						}
					}
					escribir_tmx(file_tmx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Emparella as cadeas en inglés e galego de dous ficheiros xml e vainas engadindo nun obxecto Document que representa un ficheiro xml en formato tmx
	public static void emparellar_textos(Document tmx, File file_eng, File file_gal)
	{
		try {
			Document eng = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file_eng);
			Document gal = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file_gal);
			eng.getDocumentElement().normalize();
			gal.getDocumentElement().normalize();

			NodeList listaMensaxes = eng.getElementsByTagName("message");
			NodeList listaTraducions = gal.getElementsByTagName("message");
	
			Element body = tmx.createElement("body");

			for (int i = 0; i < listaMensaxes.getLength(); i ++) {
				Node mensaxe = listaMensaxes.item(i);
				if (mensaxe.getNodeType() == Node.ELEMENT_NODE) {
			        Element elemento = (Element) mensaxe;
					int j = 0;

					while (j < listaTraducions.getLength() && !elemento.getAttribute("key").equals(((Element) listaTraducions.item(j)).getAttribute("key"))) {
						j++;
					}
					if (j < listaTraducions.getLength()) {
						Element tu, tuven, tuvgl, segen, seggl;
						tu = tmx.createElement("tu");
						tuven = tmx.createElement("tuv");
						tuven.setAttribute("xml:lang","en");
						tuvgl = tmx.createElement("tuv");
						tuvgl.setAttribute("xml:lang","gl");
						segen = tmx.createElement("seg");
						seggl = tmx.createElement("seg");

						Node datoContenido = elemento.getFirstChild();
						if(datoContenido!=null && (datoContenido.getNodeType()==Node.TEXT_NODE || datoContenido.getNodeType()==Node.CDATA_SECTION_NODE)) {
							segen.appendChild(tmx.createTextNode(datoContenido.getNodeValue()));
							seggl.appendChild(tmx.createTextNode(((Element) listaTraducions.item(j)).getFirstChild().getNodeValue()));
							tuven.appendChild(segen);
							tuvgl.appendChild(seggl);
							tu.appendChild(tuven);
							tu.appendChild(tuvgl);
							body.appendChild(tu);
						}
					}
				}
			}
			tmx.appendChild(body);

		} catch (Exception e) {
			e.printStackTrace();
		}
	} 

	// Método que pasa o contido da memoria de tradución xerada nun obxecto Document a un ficheiro xml seguindo o formato tmx
	// Código adaptado dun exemplo do blogue vidasConcurrentes
	// http://blog.vidasconcurrentes.com/programacion/tratamiento-de-xml-en-java-lectura-y-escritura/
	public static void escribir_tmx(Document tmx)
	{
		try {
			// pásase o XML ao ficheiro
			TransformerFactory transFact = TransformerFactory.newInstance();
			// engádese a sangría e a cabeceira do XML
			transFact.setAttribute("indent-number", new Integer(3));
			Transformer trans = transFact.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			// faise a transformación
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			DOMSource domSource = new DOMSource(tmx);
			trans.transform(domSource, sr);
			// crease ficheiro para escribir en modo texto
			PrintWriter writer = new PrintWriter(new FileWriter("tmx_ojs.tmx"));
			// escríbese toda a árbore no ficheiro
			writer.println(sw.toString());
			// pechase o ficheiro
			writer.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}

