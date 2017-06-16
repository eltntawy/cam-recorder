
package com.cam.recorder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;



public class FFmpeg {

	private  String ip="";
	private  int port=0;
	private  String videoDevice="";
	private  String audioDevice="";
	private  List<String>devices=null; 


	public FFmpeg() {

	}
    
	public FFmpeg(String ip, int port, String videoDevice, String audioDevice) {
		this.ip = ip;
		this.port = port;
		this.videoDevice = videoDevice;
		this.audioDevice = audioDevice;
	}

	public void executeFFmpeg() throws Exception {
		String commande = "ffmpeg -f dshow -i video=^\""+videoDevice+"^\":audio=^\""+audioDevice+"^\" -vcodec libx264 -preset ultrafast -tune zerolatency -r 30 -async 1 -acodec libmp3lame -ab 24k -ar 22050 -bsf:v h264_mp4toannexb -maxrate 750k -bufsize 3000k  -f mpegts udp://"+ip+":"+port;
		System.out.println(commande);
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", commande.toString());
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while (true) {
			line = r.readLine();
			if (line == null) { 
				break; 
			}
			System.out.println(line);
		}
		r.close();

	}

	public void executeFFmpeg(String ip, int port, String videoDevice, String audioDevice) throws Exception {
		String commande = "ffmpeg -f dshow -i video=^\""+videoDevice+"^\":audio=^\""+audioDevice+"^\" -vcodec libx264 -preset ultrafast -tune zerolatency -r 30 -async 1 -acodec libmp3lame -ab 24k -ar 22050 -bsf:v h264_mp4toannexb -maxrate 750k -bufsize 3000k  -f mpegts udp://"+ip+":"+port;
		System.out.println(commande);
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", commande.toString());
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while (true) {
			
			line = r.readLine();
			if (line == null) { 
				
				break; 
			}
			System.out.println(line);
		}
		r.close();

	}
	
	public List<String> listDiveces() throws IOException{

		String commande = "ffmpeg -list_devices true -f dshow -i dummy";
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", commande.toString());
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		List<String> ds = new ArrayList<String>();
		List<String> ds1 = new ArrayList<String>();
		while (true) {
			
			line = r.readLine();
			if (line == null) { 
				
				break; 
			}
			if(line.contains("dshow")){
				ds.add(line);
			}
			r.close();
		}
		if(!ds.isEmpty()){
			
			StringTokenizer st = new  StringTokenizer("");
			for(int i=0;i<ds.size();i++){
				
				st = new StringTokenizer(ds.get(i) ,"]");
				
				while (st.hasMoreTokens()){
					
					ds1.add(st.nextToken());
					
				}
					
			}
			ds.clear();
			for(int i=0;i<ds1.size();i++){
				
				if(!ds1.get(i).contains("[")&&ds1.get(i).contains("\"")){
					
					ds.add(ds1.get(i));	
				}		
			}
			for(int i=0;i<ds.size();i++){
				
				System.out.println(ds.get(i));
				ds.set( i, ds.get(i).replaceAll("  ", ""));
				ds.set(i,ds.get(i).replaceAll("\"", ""));
				if(ds.get(i).contains("Ã©"))
					ds.set(i, ds.get(i).replace("Ã©", "\u00e9"));
				System.out.println(ds.get(i));

			}
		}
		return ds;
	}

	public String getVideoDevice() {
		return videoDevice;
	}

	public void setVideoDevice(String videoDevice) {
		this.videoDevice = videoDevice;
	}

	public String getAudioDevice() {
		return audioDevice;
	}

	public void setAudioDevice(String audioDevice) {
		this.audioDevice = audioDevice;
	}

	public List<String> getDevices() {
		return devices;
	}

	public void setDevices(List<String> devices) {
		this.devices = devices;
	}

}
