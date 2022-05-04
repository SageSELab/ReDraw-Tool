/*******************************************************************************
 * Copyright (c) 2017, SEMERU
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/
package edu.semeru.redraw.synthetic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.redraw.uiautomator.tree.UiTreeNode;
import edu.semeru.redraw.model.Triplet;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 */
public class SyntheticViolation {

    private final static String EMPTY_STRING = "---";

    private String orgXMLFile;
    private String orgSSFile;
	private String idXml;
    private String componentType;
    private int originalX;
    private int originalY;
    private int originalWidth;
    private int originalHeight;
    private String originalText;
    private String originalColor;
    private String newX;
    private String newY;
    private String newWidth;
    private String newHeight;
    private String newText;
    private String newFont;
    private String newColor;
    private String injectionType;
    private List<Triplet<Integer, Integer, Color>> pixels = new ArrayList<Triplet<Integer, Integer, Color>>();
    private UiTreeNode node;

    private SyntheticViolation(SyntheticBuilder builder) {
        this.idXml = builder.idXml;
        this.componentType = builder.componentType;
        this.originalX = builder.originalX;
        this.originalY = builder.originalY;
        this.originalWidth = builder.originalWidth;
        this.originalHeight = builder.originalHeight;
        this.originalText = builder.originalText;
        this.setOriginalColor(builder.originalColor);
        this.newText = builder.newText;
        this.newFont = builder.newFont;
        this.newColor = builder.newColor;
        switch (builder.injectionType) {
        case SyntheticHelper.NUMBER_COMPONENTS:
            this.newX = EMPTY_STRING;
            this.newY = EMPTY_STRING;
            this.newWidth = EMPTY_STRING;
            this.newHeight = EMPTY_STRING;
            // this.newText = EMPTY_STRING;
            // this.newFont = EMPTY_STRING;
            break;
        default:
            this.newX = builder.newX + "";
            this.newY = builder.newY + "";
            this.newWidth = builder.newWidth + "";
            this.newHeight = builder.newHeight + "";
            break;
        }
        this.injectionType = builder.injectionType;
    }

    public static class SyntheticBuilder {
        private final String idXml;
        private final String componentType;
        private final int originalX;
        private final int originalY;
        private final int originalWidth;
        private final int originalHeight;
        private String originalText;
        private String originalColor;
        private int newX;
        private int newY;
        private int newWidth;
        private int newHeight;
        private String newText;
        private String newFont;
        private String newColor;
        private String injectionType;

        /**
         * @param idXml
         * @param componentType
         * @param originalX
         * @param originalY
         * @param originalWidth
         * @param originalHeight
         */
        public SyntheticBuilder(String idXml, String componentType, int originalX, int originalY, int originalWidth,
                int originalHeight) {
            super();
            this.idXml = idXml;
            this.componentType = componentType;
            this.originalX = originalX;
            this.originalY = originalY;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
            this.originalText = EMPTY_STRING;
            this.originalColor = EMPTY_STRING;
            this.newText = EMPTY_STRING;
            this.newFont = EMPTY_STRING;
            this.newColor = EMPTY_STRING;
        }

        /**
         * 
         * @param idXml
         * @param componentType
         * @param originalX
         * @param originalY
         * @param originalWidth
         * @param originalHeight
         * @param originalText
         */
        public SyntheticBuilder(String idXml, String componentType, int originalX, int originalY, int originalWidth,
                int originalHeight, String originalText) {
            super();
            this.idXml = idXml;
            this.componentType = componentType;
            this.originalX = originalX;
            this.originalY = originalY;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
            this.originalText = originalText;
            this.originalColor = EMPTY_STRING;
            this.newText = EMPTY_STRING;
            this.newFont = EMPTY_STRING;
            this.newColor = EMPTY_STRING;
        }

        public SyntheticBuilder originalColor(String originalColor) {
            this.originalColor = originalColor;
            return this;
        }

        public SyntheticBuilder newX(int newX) {
            this.newX = newX;
            return this;
        }

        public SyntheticBuilder newY(int newY) {
            this.newY = newY;
            return this;
        }

        public SyntheticBuilder newWidth(int newWidth) {
            this.newWidth = newWidth;
            return this;
        }

        public SyntheticBuilder newHeight(int newHeight) {
            this.newHeight = newHeight;
            return this;
        }

        public SyntheticBuilder newText(String newText) {
            this.newText = newText;
            return this;
        }

        public SyntheticBuilder newFont(FontType font) {
            this.newFont = font.toString();
            return this;
        }

        public SyntheticBuilder newColor(String color) {
            this.newColor = color;
            return this;
        }

        public SyntheticBuilder injectionType(String injectionType) {
            this.injectionType = injectionType;
            return this;
        }

        public SyntheticViolation build() {
            return new SyntheticViolation(this);
        }

    }

    /**
     * @return the idXml
     */
    public String getIdXml() {
        return idXml;
    }

    /**
     * @param idXml
     *            the idXml to set
     */
    public void setIdXml(String idXml) {
        this.idXml = idXml;
    }

    /**
     * @return the componentType
     */
    public String getComponentType() {
        return componentType;
    }

    /**
     * @param componentType
     *            the componentType to set
     */
    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    /**
     * @return the originalX
     */
    public int getOriginalX() {
        return originalX;
    }

    /**
     * @param originalX
     *            the originalX to set
     */
    public void setOriginalX(int originalX) {
        this.originalX = originalX;
    }

    /**
     * @return the originalY
     */
    public int getOriginalY() {
        return originalY;
    }

    /**
     * @param originalY
     *            the originalY to set
     */
    public void setOriginalY(int originalY) {
        this.originalY = originalY;
    }

    /**
     * @return the originalWidth
     */
    public int getOriginalWidth() {
        return originalWidth;
    }

    /**
     * @param originalWidth
     *            the originalWidth to set
     */
    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }

    /**
     * @return the originalHeight
     */
    public int getOriginalHeight() {
        return originalHeight;
    }

    /**
     * @param originalHeight
     *            the originalHeight to set
     */
    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }

    /**
     * @return the originalText
     */
    public String getOriginalText() {
        return originalText;
    }

    /**
     * @param originalText
     *            the originalText to set
     */
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    /**
     * @return the newX
     */
    public String getNewX() {
        return newX;
    }

    /**
     * @param newX
     *            the newX to set
     */
    public void setNewX(String newX) {
        this.newX = newX;
    }

    /**
     * @return the newY
     */
    public String getNewY() {
        return newY;
    }

    /**
     * @param newY
     *            the newY to set
     */
    public void setNewY(String newY) {
        this.newY = newY;
    }

    /**
     * @return the newWidth
     */
    public String getNewWidth() {
        return newWidth;
    }

    /**
     * @param newWidth
     *            the newWidth to set
     */
    public void setNewWidth(String newWidth) {
        this.newWidth = newWidth;
    }

    /**
     * @return the newHeight
     */
    public String getNewHeight() {
        return newHeight;
    }

    /**
     * @param newHeight
     *            the newHeight to set
     */
    public void setNewHeight(String newHeight) {
        this.newHeight = newHeight;
    }

    /**
     * @return the newText
     */
    public String getNewText() {
        return newText;
    }

    /**
     * @param newText
     *            the newText to set
     */
    public void setNewText(String newText) {
        this.newText = newText;
    }

    /**
     * @return the injectionType
     */
    public String getInjectionType() {
        return injectionType;
    }

    /**
     * @param injectionType
     *            the injectionType to set
     */
    public void setInjectionType(String injectionType) {
        this.injectionType = injectionType;
    }

    /**
     * @return the pixels
     */
    public List<Triplet<Integer, Integer, Color>> getPixels() {
        return pixels;
    }

    /**
     * @param pixels
     *            the pixels to set
     */
    public void setPixels(List<Triplet<Integer, Integer, Color>> pixels) {
        this.pixels = pixels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
        result = prime * result + ((idXml == null) ? 0 : idXml.hashCode());
        result = prime * result + originalHeight;
        result = prime * result + ((originalText == null) ? 0 : originalText.hashCode());
        result = prime * result + originalWidth;
        result = prime * result + originalX;
        result = prime * result + originalY;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SyntheticViolation other = (SyntheticViolation) obj;
        if (componentType == null) {
            if (other.componentType != null)
                return false;
        } else if (!componentType.equals(other.componentType))
            return false;
        if (idXml == null) {
            if (other.idXml != null)
                return false;
        } else if (!idXml.equals(other.idXml))
            return false;
        if (originalHeight != other.originalHeight)
            return false;
        if (originalText == null) {
            if (other.originalText != null)
                return false;
        } else if (!originalText.equals(other.originalText))
            return false;
        if (originalWidth != other.originalWidth)
            return false;
        if (originalX != other.originalX)
            return false;
        if (originalY != other.originalY)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getOrgXMLFile() + "," + this.getIdXml() + "," + this.getComponentType() + "," + this.getOriginalX()
                + "," + this.getOriginalY() + "," + this.getOriginalWidth() + "," + this.getOriginalHeight() + ","
                + this.getOriginalText() + "," + this.getOriginalColor() + "," + this.getNewX() + "," + this.getNewY()
                + "," + this.getNewWidth() + "," + this.getNewHeight() + "," + this.getNewText() + ","
                + this.getNewFont() + "," + this.getNewColor() + "," + this.getInjectionType();
    }

    /**
     * @return the newFont
     */
    public String getNewFont() {
        return newFont;
    }

    /**
     * @param newFont
     *            the newFont to set
     */
    public void setNewFont(String newFont) {
        this.newFont = newFont;
    }

    /**
     * @return the newColor
     */
    public String getNewColor() {
        return newColor;
    }

    /**
     * @param newColor
     *            the newColor to set
     */
    public void setNewColor(String newColor) {
        this.newColor = newColor;
    }

    /**
     * @return the originalColor
     */
    public String getOriginalColor() {
        return originalColor;
    }

    /**
     * @param originalColor
     *            the originalColor to set
     */
    public void setOriginalColor(String originalColor) {
        this.originalColor = originalColor;
    }

    /**
     * @return the nameFile
     */
    public String getOrgXMLFile() {
        return orgXMLFile;
    }

    /**
     * @param nameFile
     *            the nameFile to set
     */
    public void setOrgXMLFile(String nameFile) {
        this.orgXMLFile = nameFile;
    }

    /**
     * @return the node
     */
    public UiTreeNode getNode() {
        return node;
    }

    /**
     * @param node the node to set
     */
    public void setNode(UiTreeNode node) {
        this.node = node;
    }

    /**
   	 * @return the orgSSFile
   	 */
   	public String getOrgSSFile() {
   		return orgSSFile;
   	}

   	/**
   	 * @param orgSSFile the orgSSFile to set
   	 */
   	public void setOrgSSFile(String orgSSFile) {
   		this.orgSSFile = orgSSFile;
   	}
    
}
