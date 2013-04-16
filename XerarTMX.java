import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/*
Este aplicativo lee dous ficheiros xml, un coas cadeas en inglés e outro coas cadeas en galego, que seguen a dtd locale do proxecto ojs, e crea unha memoria de tradución emparellando as cadeas de inglés coa súa tradución ao galego, nun ficheiro tmx.

Tívose que retirar a dtd locale da cabeceira dos ficheiros xml porque se producía un erro ao non atopala o parser.
<!DOCTYPE locale SYSTEM "../../lib/pkp/dtd/locale.dtd">
*/
public class XerarTMX {

	public static void main(String argv[]) {
		Document eng, gal, tmx;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			eng = dBuilder.parse(new File("default.xml"));
			eng.getDocumentElement().normalize();
			gal = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("default-gl.xml"));
			tmx = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
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

			// Esta parte final de código está adaptada a este caso baseándose no código
			// realizado no seguinte blogue de vidasConcurrentes
			// http://blog.vidasconcurrentes.com/programacion/tratamiento-de-xml-en-java-lectura-y-escritura/ 
			try {
				// volcase o XML ao ficheiro
				TransformerFactory transFact = TransformerFactory.newInstance();
				// engadese identado e a cabeceira do XML
				transFact.setAttribute("indent-number", new Integer(3));
				Transformer trans = transFact.newTransformer();
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
				trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				// faise a transformacion
				StringWriter sw = new StringWriter();
				StreamResult sr = new StreamResult(sw);
				DOMSource domSource = new DOMSource(tmx);
				trans.transform(domSource, sr);
				try {
					// crease ficheiro para escribir en modo texto
					PrintWriter writer = new PrintWriter(new FileWriter("tmx_ojs.tmx"));
					// escribese toda a árbore no ficheiro
					writer.println(sw.toString());
					// pechase o ficheiro
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

