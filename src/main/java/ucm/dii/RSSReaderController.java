package ucm.dii;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.w3c.dom.events.EventTarget;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.management.Notification;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class RSSReaderController {
	
	@RequestMapping("/rss/{item}")
	public String getItemCount(@PathVariable("item") String item) throws IOException {
				
		if (!item.matches("^[A-Za-z0-9]+$"))
			return "Wrong item";
	 // code from https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
	 URL url = new URL("https://tecnologiasostenibilidad.blogspot.com/feeds/posts/default/-/"+item+"?alt=rss");
     BufferedReader in = new BufferedReader(
     new InputStreamReader(url.openStream()));
     String inputLine;     
     int count=0;
     while ((inputLine = in.readLine()) != null)
    	 count=count+inputLine.split("/item").length;
     in.close();	 
	return ""+(count-1);
	}   

	@GetMapping(value = "/rss/generacsv", produces = "text/csv")
	public ResponseEntity getCSV() throws IOException {		
		String item="sostenibilidad";
		 // code from https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
		 URL url = new URL("https://tecnologiasostenibilidad.blogspot.com/feeds/posts/default/-/"+item+"?alt=rss");
	     BufferedReader in = new BufferedReader(
	     new InputStreamReader(url.openStream()));
	     String inputLine;     
	     int count=0;
	     StringBuffer sb=new StringBuffer();
	     while ((inputLine = in.readLine()) != null)
	    	 sb.append(inputLine);
	     in.close();	 
	     String contenido=sb.toString();

	     String contenidoODS="";
	    for (int k=1;k<=17;k++){
	        contenidoODS=contenidoODS+"ODS"+k+","+contenido.split("ODS"+k+"<").length+"\n";
	    }
	    String fichero="ODS.csv";
	    
	   return  ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=" + fichero )
        .contentLength(contenidoODS.getBytes().length)
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(new ByteArrayResource(contenidoODS.getBytes()));
	     		
	
	}     
	
	
	@RequestMapping(value = "/rss/generachart")
	public ResponseEntity getChart() throws IOException {	
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        	 
		 
		String item="sostenibilidad";
		 // code from https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
		 URL url = new URL("https://tecnologiasostenibilidad.blogspot.com/feeds/posts/default/-/"+item+"?alt=rss");
	     BufferedReader in = new BufferedReader(
	     new InputStreamReader(url.openStream()));
	     String inputLine;     
	     int count=0;
	     StringBuffer sb=new StringBuffer();
	     while ((inputLine = in.readLine()) != null)
	    	 sb.append(inputLine);
	     in.close();	 
	     String contenido=sb.toString();

	   
	    for (int k=1;k<=17;k++){
	    	 dataset.addValue(contenido.split("ODS"+k+"<").length, "ODS", "ODS"+k);
	    }
	    
	    
	     JFreeChart chart = ChartFactory.createBarChart(
				 "Progreso ODS",
	                "ODS", "#Acciones", dataset,
	                PlotOrientation.VERTICAL,
	                false,
	                true,
	                false);
	    chart.setBackgroundPaint(Color.white);
	     CategoryAxis domain = chart.getCategoryPlot().getDomainAxis();
	     domain.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
	    BufferedImage img = chart.createBufferedImage(600, 300); 
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(img, "PNG", baos);
	    return new ResponseEntity<byte[]>(baos.toByteArray(),
	    		HttpStatus.OK);
	
	}     
	
	@RequestMapping("/rss/resumen")
	public String getSummary() throws IOException {		
	 String item="sostenibilidad";
	 // code from https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
	 URL url = new URL("https://tecnologiasostenibilidad.blogspot.com/feeds/posts/default/-/"+item+"?alt=rss");
     BufferedReader in = new BufferedReader(
     new InputStreamReader(url.openStream()));
     String inputLine;     
     int count=0;
     StringBuffer sb=new StringBuffer();
     while ((inputLine = in.readLine()) != null)
    	 sb.append(inputLine);
     in.close();	 
     String contenido=sb.toString();

     String contenidoODS="";
    for (int k=1;k<=17;k++){
        contenidoODS=contenidoODS+"ODS"+k+":"+contenido.split("ODS"+k+"<").length+";";
    }
     
     
	return  "Total acciones (algunas acciones ODS pueden solapar): "+contenido.split("/item").length+"\n"+
	        "Relación de acciones con ODS:\n"+
            contenidoODS;
	
	}    


}
