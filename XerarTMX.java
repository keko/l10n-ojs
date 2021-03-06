import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/*
Agora o aplicativo lee dous ficheiros que se lle pasan como argumentos por consola xunto co código de idioma de orixe e o de destino. Estes ficheiros conteñen a lista de ficheiros que hai que emparellar. En ver de emparellar dous ficheiros, emparella un listado de ficheiros xml. Por agora non admite todos os ficheiros xml do proxecto ojs e crea unha memoria de tradución en formato tmx.

Soporte para as dtd: locale, toc, email_texts, currencies, countries e topic

Por agora o código consta dos seguintes métodos:
- emparellar_textos
- emparellar_locale
- emparellar_toc
- emparellar_email_texts
- emparellar_currencies
- emparellar_countries
- emparellar_topic
- escribir_tmx
- engadirTupla


TODO
- Darlle soporte a máis dtd que se usan nos ficheiros de tradución do proxecto ojs: só falta version.
- Explicar que se realiza nas diferentes partes do código.
- Mellorar a cabeceira do ficheiro tmx xerado para adaptala ao estándar.
*/


public class XerarTMX {

	static String sourceLocale, targetLocale;

	public static void main(String argv[])
	{
		if (argv.length == 4)
		{
			File files_eng = new File(argv[0]);
			File files_gal = new File(argv[1]);
			// Variable que almacena o código do idioma de orixe
			sourceLocale = argv[2];
			// Variable que almacena o código do idioma de destino
			targetLocale = argv[3];
			if (files_eng.exists() && files_gal.exists())
			{
				Document file_tmx;
				BufferedReader bfiles_eng, bfiles_gal;
				try
				{
					file_tmx = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					Element ebody = file_tmx.createElement("body");
					bfiles_eng = new BufferedReader(new FileReader(files_eng));
					bfiles_gal = new BufferedReader(new FileReader(files_gal));
					String sfile_eng, sfile_gal;
					while(bfiles_eng.ready() && bfiles_gal.ready())
					{
						sfile_eng = bfiles_eng.readLine();
						sfile_gal = bfiles_gal.readLine();
						File ffile_eng, ffile_gal;
						ffile_eng = new File(sfile_eng);
						ffile_gal = new File(sfile_gal);
						if (ffile_eng.exists() && ffile_gal.exists())
							emparellar_textos(file_tmx,ebody, ffile_eng,ffile_gal);
					}
					file_tmx.appendChild(ebody);
					escribir_tmx(file_tmx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else
			System.out.println("Usage: java XerarTMX <list-source-files> <list-target-files> <source-locale> <target-locale>\n\nXerarTMX: error: You need to give four parameters. A files list of source, a files list of target, a locale code of source and an locale code of target\n\nFor example: java XerarTMX files-xml-en_US files-xml-gl_ES en_US gl_ES");
	}

	// Emparella as cadeas en inglés e galego de dous ficheiros xml e vainas engadindo nun obxecto Document que representa un ficheiro xml en formato tmx
	public static void emparellar_textos(Document tmx, Element body, File file_eng, File file_gal) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// Como desactivar as dtd para non validar os ficheiros.
			// http://stackoverflow.com/questions/243728/how-to-disable-dtd-at-runtime-in-javas-xpath
			builder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					System.out.println("Ignorase a dtd " + publicId + ", " + systemId);
					return new InputSource(new StringReader(""));
				}
			});
			Document deng = builder.parse(file_eng);
			Document dgal = builder.parse(file_gal);
			deng.getDocumentElement().normalize();
			dgal.getDocumentElement().normalize();
			String dtd = deng.getDoctype().getName();
			if (dtd.equals("locale"))
				emparellar_locale(tmx,body,deng,dgal);
			else
				if (dtd.equals("toc"))
					emparellar_toc(tmx,body,deng,dgal);
				else
					if (dtd.equals("email_texts"))
						emparellar_email_texts(tmx,body,deng,dgal);
					else
						if (dtd.equals("currencies"))
							emparellar_currencies(tmx,body,deng,dgal);
						else
							if (dtd.equals("countries"))
								emparellar_countries(tmx,body,deng,dgal);
							else
								if (dtd.equals("topic"))
									emparellar_topic(tmx,body,deng,dgal);
								else
									System.out.println("Falta tratar estas dtds: " + dtd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 


	public static void engadirTupla(Document tm, Element bd, String source, String target)
	{
		Element etiquetaTu, etiquetaTuvSource, etiquetaTuvTarget, etiquetaSegSource, etiquetaSegTarget;
		etiquetaTu = tm.createElement("tu");
		etiquetaTuvSource = tm.createElement("tuv");
		etiquetaTuvSource.setAttribute("xml:lang",sourceLocale);
		etiquetaTuvTarget = tm.createElement("tuv");
		etiquetaTuvTarget.setAttribute("xml:lang",targetLocale);
		etiquetaSegSource = tm.createElement("seg");
		etiquetaSegTarget = tm.createElement("seg");
		etiquetaSegSource.appendChild(tm.createTextNode(source));
		etiquetaSegTarget.appendChild(tm.createTextNode(target));
		etiquetaTuvSource.appendChild(etiquetaSegSource);
		etiquetaTuvTarget.appendChild(etiquetaSegTarget);
		etiquetaTu.appendChild(etiquetaTuvSource);
		etiquetaTu.appendChild(etiquetaTuvTarget);
		bd.appendChild(etiquetaTu);
	}


	public static void emparellar_locale(Document tmx, Element body, Document eng, Document gal) {
		try {
			NodeList listaMensaxes = eng.getElementsByTagName("message");
			NodeList listaTraducions = gal.getElementsByTagName("message");

			for (int i = 0; i < listaMensaxes.getLength(); i ++) {
				Node mensaxe = listaMensaxes.item(i);
				if (mensaxe.getNodeType() == Node.ELEMENT_NODE) {
				    Element elemento = (Element) mensaxe;
					int j = 0;
					while (j < listaTraducions.getLength() && !elemento.getAttribute("key").equals(((Element) listaTraducions.item(j)).getAttribute("key"))) {
						j++;
					}
					if (j < listaTraducions.getLength()) {
						Node datoContenido = elemento.getFirstChild();
						if(datoContenido!=null && (datoContenido.getNodeType()==Node.TEXT_NODE || datoContenido.getNodeType()==Node.CDATA_SECTION_NODE))
							engadirTupla(tmx,body,datoContenido.getNodeValue(),((Element) listaTraducions.item(j)).getFirstChild().getNodeValue());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void emparellar_toc(Document tmx, Element body, Document eng, Document gal) {
		try {
			NodeList listaTopic_source = eng.getElementsByTagName("topic");
			NodeList listaTopic_target = gal.getElementsByTagName("topic");	
			for (int i = 0; i < listaTopic_source.getLength(); i ++)
			{
				Node topic = listaTopic_source.item(i);
				if (topic.getNodeType() == Node.ELEMENT_NODE)
				{
			        Element elemento = (Element) topic;
					int j = 0;
					while (j < listaTopic_target.getLength() && !elemento.getAttribute("id").equals(((Element) listaTopic_target.item(j)).getAttribute("id")))
						j++;
					if (j < listaTopic_target.getLength())
						engadirTupla(tmx,body,elemento.getAttribute("title"),((Element) listaTopic_target.item(j)).getAttribute("title"));
				}
			}
			NodeList listaBreadcrumb_source = eng.getElementsByTagName("breadcrumb");
			NodeList listaBreadcrumb_target = gal.getElementsByTagName("breadcrumb");
			for (int i = 0; i < listaBreadcrumb_source.getLength(); i ++)
			{
				Node breadcrumb = listaBreadcrumb_source.item(i);
				if (breadcrumb.getNodeType() == Node.ELEMENT_NODE)
				{
			        Element elemento = (Element) breadcrumb;
					int j = 0;
					while (j < listaBreadcrumb_target.getLength() && !elemento.getAttribute("url").equals(((Element) listaBreadcrumb_target.item(j)).getAttribute("url")))
						j++;
					if (j < listaBreadcrumb_target.getLength())
						engadirTupla(tmx,body,elemento.getAttribute("title"),((Element) listaBreadcrumb_target.item(j)).getAttribute("title"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void emparellar_email_texts(Document tmx, Element body, Document eng, Document gal) {
		try {
			NodeList listaEmail_source = eng.getElementsByTagName("email_text");
			NodeList listaEmail_target = gal.getElementsByTagName("email_text");	
			for (int i = 0; i < listaEmail_source.getLength(); i ++)
			{
				Node email_text = listaEmail_source.item(i);
				if (email_text.getNodeType() == Node.ELEMENT_NODE)
				{
			        Element elemento = (Element) email_text;
					int j = 0;
					while (j < listaEmail_target.getLength() && !elemento.getAttribute("key").equals(((Element) listaEmail_target.item(j)).getAttribute("key")))
					{
						j++;
					}
					if (j < listaEmail_target.getLength())
					{
						NodeList campos_source = email_text.getChildNodes();
						NodeList campos_target = listaEmail_target.item(j).getChildNodes(); 
						for (int k = 0; k < campos_source.getLength(); k++) {
							if (campos_source.item(k).getNodeType() == Node.ELEMENT_NODE && campos_target.item(k).hasChildNodes())
								engadirTupla(tmx,body,campos_source.item(k).getFirstChild().getNodeValue(),campos_target.item(k).getFirstChild().getNodeValue());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void emparellar_currencies(Document tmx, Element body, Document eng, Document gal) {
		try {
			NodeList listaCurrency_source = eng.getElementsByTagName("currency");
			NodeList listaCurrency_target = gal.getElementsByTagName("currency");	
			for (int i = 0; i < listaCurrency_source.getLength(); i ++)
			{
				Node currency = listaCurrency_source.item(i);
				if (currency.getNodeType() == Node.ELEMENT_NODE)
				{
			        Element elemento = (Element) currency;
					int j = 0;
					while (j < listaCurrency_target.getLength() && !elemento.getAttribute("code_alpha").equals(((Element) listaCurrency_target.item(j)).getAttribute("code_alpha")))
					{
						j++;
					}
					if (j < listaCurrency_target.getLength())
						engadirTupla(tmx,body,elemento.getAttribute("name"),((Element) listaCurrency_target.item(j)).getAttribute("name"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void emparellar_countries(Document tmx, Element body, Document eng, Document gal) {
		try {
			NodeList listaCountry_source = eng.getElementsByTagName("country");
			NodeList listaCountry_target = gal.getElementsByTagName("country");	
			for (int i = 0; i < listaCountry_source.getLength(); i ++)
			{
				Node country = listaCountry_source.item(i);
				if (country.getNodeType() == Node.ELEMENT_NODE)
				{
			        Element elemento = (Element) country;
					int j = 0;
					while (j < listaCountry_target.getLength() && !elemento.getAttribute("code").equals(((Element) listaCountry_target.item(j)).getAttribute("code")))
					{
						j++;
					}
					if (j < listaCountry_target.getLength())
						engadirTupla(tmx,body,elemento.getAttribute("name"),((Element) listaCountry_target.item(j)).getAttribute("name"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void emparellar_topic(Document tmx, Element body, Document eng, Document gal) {
		try {
			NodeList listaSectionSource = eng.getElementsByTagName("section");
			NodeList listaSectionTarget = gal.getElementsByTagName("section");
			for (int i = 0; i < listaSectionSource.getLength() && i < listaSectionTarget.getLength(); i ++) {
				Node sectionSource = listaSectionSource.item(i);
				Node sectionTarget = listaSectionTarget.item(i);
				if (sectionSource.hasAttributes() && sectionSource.getNodeType() == Node.ELEMENT_NODE && sectionTarget.hasAttributes() && sectionTarget.getNodeType() == Node.ELEMENT_NODE)
					engadirTupla(tmx,body,((Element) sectionSource).getAttribute("title"),((Element) sectionTarget).getAttribute("title"));
				NodeList source = sectionSource.getChildNodes();
				NodeList target = sectionTarget.getChildNodes();
				for (int j = 0; j < source.getLength() && j < target.getLength(); j ++)
				{
					if (source.item(j).getNodeType() == Node.CDATA_SECTION_NODE && target.item(j).getNodeType() == Node.CDATA_SECTION_NODE)
						engadirTupla(tmx,body,source.item(j).getNodeValue(),target.item(j).getNodeValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// Método que pasa o contido da memoria de tradución xerada nun obxecto Document a un ficheiro xml seguindo o formato tmx
	// Código adaptado dun exemplo do blogue vidasConcurrentes
	// http://blog.vidasconcurrentes.com/programacion/tratamiento-de-xml-en-java-lectura-y-escritura/
	public static void escribir_tmx(Document tmx) {
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
			PrintWriter writer = new PrintWriter(new FileWriter("tmxOJS_"+sourceLocale+"_"+targetLocale+".tmx"));
			// escríbese toda a árbore no ficheiro
			writer.println(sw.toString());
			// pechase o ficheiro
			writer.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}

