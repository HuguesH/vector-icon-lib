package org.architecture.drawio;

import java.util.logging.Logger;

public class MxLibrary {
    private static final Logger logger = Logger.getLogger(MxLibrary.class.getName());

    String name;
    MxIcon[] icons;

    public MxLibrary(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MxIcon[] getIcons() {
        return icons;
    }

    public void setIcons(MxIcon[] icons) {
        this.icons = icons;
    }

}
