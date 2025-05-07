package org.architecture.drawio;

import java.util.Base64;

public class MxIcon {
    String xml;
    String data;
    String aspect;
    String title;
    String w;
    String h;

    public MxIcon(){

    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = String.valueOf(Math.round(Float.parseFloat(w)));
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = String.valueOf(Math.round(Float.parseFloat(h)));
    }

    public boolean isGraphModel(){
        return xml != null;
    }

    public boolean isImage(){
        return data != null;
    }

    public boolean isSvgImage(){
        if(isImage()){
            return data.startsWith("data:image/svg");
        }
        return false;
    }

    public String getSvgPayload(){
        if (isSvgImage()){
            return new String(Base64.getDecoder().decode(data.split(",")[1]));
        }

        return null;
    }
}
