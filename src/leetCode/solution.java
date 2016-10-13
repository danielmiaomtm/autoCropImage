package autoCropImage;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray; 
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.Rectangle ; 
import java.io.File;  
import java.io.IOException;  


public class solution {
	
	static String test_URL = "https://test.flaunt.peekabuy.com/api/board/get_jc_product_images_batch/?page=2";
	static List<String> background; 
	static List<String> category;
	static List<String> images;
	static int code;
	static String message;
	
	public static void main(String[] args) throws Exception {
// get the JSON data from URL, and store in local 	
		background = new ArrayList<>();
		category = new ArrayList<>();
		images = new ArrayList<>();
		JSONObject jsonobj = new JSONObject(callURL(test_URL));   		   
		message = jsonobj.getString("message");   		 
		code = jsonobj.getInt("code");   
		 
		JSONArray jsonarray = jsonobj.getJSONArray("images");   
		ArrayList<String> listdata = new ArrayList<String>();     
		if (jsonarray != null) { 
		   for (int i=0;i<jsonarray.length();i++){ 
		    listdata.add(jsonarray.get(i).toString());
		   } 
		} 

		for (int i = 0; i < listdata.size(); i++) {
			String[] temp = listdata.get(i).split(",");
			background.add(temp[2]);
			category.add(temp[1]);
			images.add(temp[0]);
		}
		
		
		for (int i = 0; i < 10; i++) {
			int len = images.get(i).length();
			String imageUrl = images.get(i).substring(2, len - 1);
			if (background.get(i) == "2") {
//clean
				pureBackGround(imageUrl, i);
			} else {
//unclean
				
			}
			
		}
		
//test URL		
//		mainFunction("https://peekabuy.s3.amazonaws.com/products/image/5d6ecd2089099608f836b3e417773022.jpg", 0);
		
		
		System.out.println("done");	
	}
	 
	
	
// pureBackground 		
	public static void pureBackGround (String imageUrl, int index) throws IOException {
		
//download the image to local			
				int len = imageUrl.length();
//				String newName = imageUrl.substring(49, len);
//				String destinationFile = System.getProperty("user.dir") + "/" + newName;
				
				String destinationFile = System.getProperty("user.dir") + "/" + index + ".jpg";
				
				
				saveImageURL(imageUrl, destinationFile);	 

		//// get the image from file	
				
				BufferedImage img = null;
				try {
				    img = ImageIO.read(new File(destinationFile)); 
				} catch (IOException e) {
				    e.printStackTrace();
				}
				
// 			get the matrix from greyscale image
//			System.out.println("before width  " + img.getWidth());
//		        System.out.println("before height  " + img.getHeight());
				toGray(img);
				
//			System.out.println("after width  " + img.getWidth());
//		        System.out.println("after height  " + img.getHeight());
//			String geryFileName = System.getProperty("user.dir") + "/grey_" + newName;
				String geryFileName = System.getProperty("user.dir") + "/grey_" + index + ".jpg";
				
				saveImageBufferedImage(img, "jpg", geryFileName);
				
				Raster raster = img.getData();
				int w = img.getWidth();
				int h = img.getHeight();
//			System.out.println("after width  " + img.getWidth());
//		        System.out.println("after height  " + img.getHeight());
				int[][] imageMatrix = new int[w][h];
				
				for (int i = 0; i < w; i++) {
				    for (int j = 0; j < h; j++) {
				    	imageMatrix[i][j] = raster.getSample(i, j, 0);
				    }
				}
						
//			int[][] imageMatrix = convertTo2DWithoutUsingGetRGB(img);
				
			// return the crop frame: top, bottom, left, right 		
//				for(int[] row : imageMatrix) {
//					for (int i : row) {
//			            System.out.print(i);
//			            System.out.print("\t");
//			        }
//			        System.out.println();
//		        }
				
				
				List<Integer> cropFrame = cropImage(imageMatrix);
				System.out.println(Arrays.toString(cropFrame.toArray()));
			    
				int top = cropFrame.get(0);
				int bottom = cropFrame.get(1);
				int left = cropFrame.get(2);
				int right = cropFrame.get(3);
				
				System.out.println("top   " + top);
				System.out.println("left  " + left);
				System.out.println("bottom   " + bottom);
				System.out.println("right  " + right);

				
				int width = right - left + 1;
				int height = bottom - top + 1;
				if (width >= imageMatrix.length) {
					width = imageMatrix.length;
				}
				if (height >= imageMatrix[0].length) {
					height = imageMatrix[0].length;
				}
				
				System.out.println("width   " + width);
				System.out.println("height  " + height);


				BufferedImage originalImage = null;
				try {
					originalImage = ImageIO.read(new File(destinationFile)); 
				} catch (IOException e) {
				    e.printStackTrace();
				}
					
// get the result Image by cutting from original Image by cropFrame			
				BufferedImage resultImage = cropImage(new File(destinationFile), left, top, width, height);
//				System.out.println("done");
				
				
				
				
//				
				String resultImagePath = System.getProperty("user.dir") + "/" + "result_"+ index + ".jpg";
		
//				System.out.println(resultImagePath);
				saveImageBufferedImage(resultImage, "jpg", resultImagePath);
//				System.out.println(resultImagePath);
	
	}

	
	
// save the BufferedImage to local file 
	public static void saveImageBufferedImage (BufferedImage image, String format,  
            String filePath) {  
        try {  
            ImageIO.write(image, format, new File(filePath));  
        } catch (Exception e) {  
            throw new RuntimeException(e);  
        }  
    }  

	
	
// crop the image based on the cropFrame
	
	private static BufferedImage cropImage(File filePath, int x, int y, int w, int h){

	    try {
	        BufferedImage originalImgage = ImageIO.read(filePath);
	        BufferedImage subImgage = originalImgage.getSubimage(x, y, w, h);
	        return subImgage;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	
// get the crop frame: top, bottom, left, right 	
	public static List<Integer> cropImage (int[][] input) {
        int top = -1;
        int bottom = -1;
        int left = -1;
        int right = -1;
        
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < input.length; i++) {
            int target = input[i][0];
            for (int j = 1; j < input[0].length; j++) {
                if (target != input[i][j]) {
                    if (top == -1) {
                        top = i;
                        break;
                    }
                } 
            }
        }
        
        for (int i = input.length - 1; i >= top; i--) {
            int target = input[i][0];
            for (int j = 1; j < input[0].length; j++) {
                if (target != input[i][j]) {
                    if (bottom == -1) {
                        bottom = i;
                        break;
                    }
                } 
            }
        }
        
        for (int j = 0; j < input[0].length; j++) {
            int target = input[0][j];
            for (int i = 1; i < input.length; i++) {
                if (target != input[i][j]) {
                    if (left == -1) {
                        left = j;
                        break;
                    }
                } 
            }
        }
        
        for (int j = input[0].length - 1; j >= left; j--) {
            int target = input[0][j];
            for (int i = 1; i < input.length; i++) {
                if (target != input[i][j]) {
                    if (right == -1) {
                        right = j;
                        break;
                    }
                } 
            }
        }
        
        result.add(left);
        result.add(right);
        result.add(top);
        result.add(bottom);
        
        return result;
    
    }
	
	
	public static BufferedImage thresholdImage(BufferedImage image, int threshold) {
	    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	    result.getGraphics().drawImage(image, 0, 0, null);
	    WritableRaster raster = result.getRaster();
	    int[] pixels = new int[image.getWidth()];
	    for (int y = 0; y < image.getHeight(); y++) {
	        raster.getPixels(0, y, image.getWidth(), 1, pixels);
	        for (int i = 0; i < pixels.length; i++) {
	            if (pixels[i] < threshold) pixels[i] = 0;
	            else pixels[i] = 255;
	        }
	        raster.setPixels(0, y, image.getWidth(), 1, pixels);
	    }
	    return result;
	}
	
	
// change image to grey color
	public static void toGray(BufferedImage image) {
	    int width = image.getWidth();
	    int height = image.getHeight();
	    for(int i=0; i<height; i++){
	      for(int j=0; j<width; j++){
	        Color c = new Color(image.getRGB(j, i));
	        int red = (int)(c.getRed() * 0.21);
	        int green = (int)(c.getGreen() * 0.72);
	        int blue = (int)(c.getBlue() *0.07);
	        int sum = red + green + blue;
	        Color newColor = new Color(sum,sum,sum);
	        image.setRGB(j,i,newColor.getRGB());
	      }
	    }
	  }

	
	
// save image to local file
	
	public static void saveImageURL(String imageUrl, String destinationFile) throws IOException {
		URL url = new URL(imageUrl);
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(destinationFile);

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
	}
	
	
// call URL to get Json	
	public static String callURL(String myURL) {
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(myURL);
			urlConn = url.openConnection();
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(),
						Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
		in.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception while calling URL:"+ myURL, e);
		} 
 
		return sb.toString();
	}
		    
}
