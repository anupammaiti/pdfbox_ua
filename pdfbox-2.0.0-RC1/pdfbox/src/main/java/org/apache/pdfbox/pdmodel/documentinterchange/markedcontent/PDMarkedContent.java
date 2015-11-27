/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.documentinterchange.markedcontent;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDArtifactMarkedContent;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.text.TextPosition;

/**
 * A marked content.
 * 
 * @author Johannes Koch
 */
public class PDMarkedContent
{

    /**
     * Creates a marked-content sequence.
     * 
     * @param tag the tag
     * @param properties the properties
     * @return the marked-content sequence
     */
    public static PDMarkedContent create(COSName tag, COSDictionary properties)
    {
        if (COSName.ARTIFACT.equals(tag))
        {
            return new PDArtifactMarkedContent(properties);
        }
        return new PDMarkedContent(tag, properties);
    }


    private final String tag;
    private final COSDictionary properties;
    private final List<Object> contents;

    private Area area;
    private List<Shape> outline;
    private StringBuilder contentString;
    private String xObjectRefTag = null;
    
    /**
     * Creates a new marked content object.
     * 
     * @param tag the tag
     * @param properties the properties
     */
    public PDMarkedContent(COSName tag, COSDictionary properties)
    {
        this.tag = tag == null ? null : tag.getName();
        this.properties = properties;
        this.contents = new ArrayList<Object>();
    }


    /**
     * Gets the tag.
     * 
     * @return the tag
     */
    public String getTag()
    {
        return this.tag;
    }

    /**
     * Gets the properties.
     * 
     * @return the properties
     */
    public COSDictionary getProperties()
    {
        return this.properties;
    }

    /**
     * Gets the marked-content identifier.
     * 
     * @return the marked-content identifier, or -1 if it doesn't exist.
     */
    public int getMCID()
    {
        return this.getProperties() == null ? -1 :
            this.getProperties().getInt(COSName.MCID);
    }

    /**
     * Gets the language (Lang).
     * 
     * @return the language
     */
    public String getLanguage()
    {
        return this.getProperties() == null ? null :
            this.getProperties().getNameAsString(COSName.LANG);
    }

    /**
     * Gets the actual text (ActualText).
     * 
     * @return the actual text
     */
    public String getActualText()
    {
        return this.getProperties() == null ? null :
            this.getProperties().getString(COSName.ACTUAL_TEXT);
    }

    /**
     * Gets the alternate description (Alt).
     * 
     * @return the alternate description
     */
    public String getAlternateDescription()
    {
        return this.getProperties() == null ? null :
            this.getProperties().getString(COSName.ALT);
    }

    /**
     * Gets the expanded form (E).
     * 
     * @return the expanded form
     */
    public String getExpandedForm()
    {
        return this.getProperties() == null ? null :
            this.getProperties().getString(COSName.E);
    }

    /**
     * Gets the contents of the marked content sequence. Can be
     * <ul>
     *   <li>{@link TextPosition},</li>
     *   <li>{@link PDMarkedContent}, or</li>
     *   <li>{@link PDXObject}.</li>
     * </ul>
     * 
     * @return the contents of the marked content sequence
     */
    public List<Object> getContents()
    {
        return this.contents;
    }

    /**
     * Adds a text position to the contents.
     * 
     * @param text the text position
     */
    public void addText(TextPosition text)
    {
        this.getContents().add(text);
    }

    /**
     * Adds a marked content to the contents.
     * 
     * @param markedContent the marked content
     */
    public void addMarkedContent(PDMarkedContent markedContent)
    {
        this.getContents().add(markedContent);
    }

    /**
     * Adds an XObject to the contents.
     * 
     * @param xobject the XObject
     */
    public void addXObject(PDXObject xobject)
    {
        this.getContents().add(xobject);
    }

    public void setXObjectRefTag(COSObject xobj) {
    	this.xObjectRefTag = PDMarkedContent.createXObjectRefTag(xobj);
    }
    
    public String getXObjectRefTag() {
    	return this.xObjectRefTag;
    }

    public static final String createXObjectRefTag(COSObject obj) {
    	return obj.getObjectNumber() + "_" + obj.getGenerationNumber();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("tag=").append(this.tag)
            .append(", properties=").append(this.properties);
        sb.append(", contents=").append(this.contents);
        
        if (this.getAlternateDescription() != null) {
        	sb.append(", alt=" + this.getAlternateDescription());
        }
        
       	sb.append(", content_string=" + this.getContentString());
        return sb.toString();
    }

    public boolean isArtifact() {
    	return false;
    }

    public void appendContentString(String contentString) {
    	if (this.contentString == null) {
    		this.contentString = new StringBuilder();
    	}
    	this.contentString.append(contentString);
    }
    
    public String getContentString() {
    	if (this.contentString != null) {
    		return this.contentString.toString();
    	} else if (this.contents != null && !this.contents.isEmpty()) {
    		this.contentString = new StringBuilder();
    		for (Object obj : this.contents) {
    			if (obj instanceof PDMarkedContent) {
    				this.contentString.append(((PDMarkedContent) obj).getContentString());    				
    			}
    		}
    		return this.contentString.toString();
    	}
    	return "";
    }
    
    public void addOutlineShape(Shape shape) {
    	if (this.outline == null) {
    		this.outline = new ArrayList<Shape>();
    	}
    	this.outline.add(shape);
    }
    
    public Area getOutlineArea() {
    	if (this.area != null) {
    		return this.area;
    	}
    	this.area = new Area();
    	if (this.outline != null) {
    		for (Shape s : this.outline) {
        		this.area.add(new Area(s));
        	}    		
    	} else if (this.contents != null && !this.contents.isEmpty()) {
    		this.contentString = new StringBuilder();
    		for (Object obj : this.contents) {
    			if (obj instanceof PDMarkedContent) {
    				this.area.add(((PDMarkedContent) obj).getOutlineArea());    				
    			}
    		}
    	}
    	return this.area;
    }
}
