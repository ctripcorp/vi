package com.ctrip.framework.vi.analyzer;

import com.ctrip.framework.vi.enterprise.EnFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import java.io.InputStream;
import java.util.*;

/**
 * Created by jiang.j on 2016/5/19.
 */
public class PomDependencyHandler implements ContentHandler {

    private final String PROJECT="project";
    private final String DEPENDENCY="dependency";
    private final String GROUPID="groupId";
    private final String ARTIFACTID="artifactId";
    private final String  VERSION="version";
    private final String  SCOPE="scope";
    private String parentVersion=null;
    private String parentGroupId=null;
    private String parentArtifactId=null;
    private boolean readParent = false;
    private Map<String,String> props = new HashMap<>();
    private String tmpProName;
    private final List<String> valueElements = new ArrayList<String>(4){
        {
            add(GROUPID);
            add(ARTIFACTID);
            add(VERSION);
            add(SCOPE);
        }
    };
    private final List<PomDependency> dependencies = new ArrayList<>();
    private PomDependency current = null;
    private final StringBuilder sb = new StringBuilder();
    private boolean startReadText =false;
    private PomInfo root = null;
    private int openElement = 0;
    private boolean readProperties=false;
    private boolean parentBeHandled = false;

    private List<PomDependency> allParentDeps = null;


    public PomDependencyHandler(){

    }
    public  PomDependencyHandler(List<PomDependency> deps){
        this.allParentDeps = deps;
    }

    void addToParentDeps(PomDependency dep){

        boolean hasAdded = false;
        for(PomDependency d:allParentDeps){
            if(d.groupId.equals(dep.groupId) && d.artifactId.equals(dep.artifactId)){
                d.version = dep.version;
                if(d.scope == null && dep.scope !=null) {
                    d.scope = dep.scope;
                }
                hasAdded = true;
                break;
            }
        }
        if(!hasAdded)
        this.allParentDeps.add(dep);
    }
    private void handleParent(){

        if(parentBeHandled){
            return;
        }
        parentBeHandled = true;
        if(parentArtifactId != null){
            boolean needReadParent = false;
            if(this.allParentDeps == null) {
                for (PomDependency dep : dependencies) {
                    if (dep.version == null) {
                        needReadParent = true;
                        break;
                    }
                }
            }else{
                needReadParent = true;
            }

            if(needReadParent) {
                XMLReader xmlReader = null;
                try {
                    xmlReader = XMLReaderFactory.createXMLReader();
                    xmlReader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
                    PomDependencyHandler handler;
                    if(this.allParentDeps == null){
                        this.allParentDeps = new ArrayList<>();
                    }

                    handler = new PomDependencyHandler(this.allParentDeps);


                    xmlReader.setContentHandler(handler);
                    try(InputStream in = EnFactory.getEnMaven().getPomInfoByFileName(new String[]{
                            parentArtifactId, parentVersion,parentGroupId
                    }, null)) {
                        xmlReader.parse(new InputSource(in));
                    }
                    List<PomDependency> parentDeps = handler.dependencies;
                    for(PomDependency dep:parentDeps){
                        if(dep.version != null){
                            addToParentDeps(dep);
                        }
                    }
                    handler.handleParent();
                    for(PomDependency dep:dependencies){
                        if(dep.version == null){
                            fillVersion(this.allParentDeps,dep);
                        }
                    }
                } catch (Throwable e) {
                }
            }
        }
    }
    public List<PomDependency> getDependencies(){
        handleParent();
        return this.dependencies;
    }

    private void fillVersion(List<PomDependency> deps,PomDependency sourceDep){
        for (PomDependency dep:deps){
           if(dep.artifactId.equals(sourceDep.artifactId) && dep.groupId.equals(sourceDep.groupId)){
               sourceDep.version = dep.version;
               if(sourceDep.scope == null && dep.scope != null){
                   sourceDep.scope = dep.scope;
               }
               break;
           }
        }
    }

    public PomInfo getPomInfo(){
        if(root.version==null){
            root.version = parentVersion;
        }

        if(root.groupId==null){
            root.groupId = parentGroupId;
        }
        handleParent();
        String version = root.version;
        if(version.startsWith("${")){
            version = props.get(version.substring(2,version.length()-1));
            root.version = version;
        }
        return root;
    }
    /*
        <dependency>
            <groupId>com.ctriposs.baiji</groupId>
            <artifactId>baiji-rpc-server</artifactId>
            <version>1.6.10</version>
            <scope>compile</scope>
            */
    @Override
    public void setDocumentLocator(Locator locator) {

    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        openElement++;
        if(current!=null || (root!=null && openElement<3 && valueElements.contains(localName))){
            startReadText =true;
        }

        if(readProperties){
            tmpProName = localName;
            startReadText =true;
        }

        if(readParent && (VERSION.equals(localName)||ARTIFACTID.equals(localName)||GROUPID.equals(localName))){
            startReadText =true;
        }
        if(openElement<3 ){
            if("parent".equals(localName)) {
                readParent = true;
            }else if("properties".equals(localName)){
                readProperties = true;
            }
        }

        if(localName.equals(DEPENDENCY) && current ==null){
            current = new PomDependency();
        }

        if(localName.equals(PROJECT) && root == null){
            root = new PomInfo();
        }


    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if(openElement<3 ){
            if("parent".equals(localName)) {
                readParent = false;
            }else if("properties".equals(localName)){
                readProperties = false;
            }
        }

        if(readParent && (VERSION.equals(localName)||ARTIFACTID.equals(localName)||GROUPID.equals(localName)) && startReadText){
            if(VERSION.equals(localName)) {
                parentVersion = sb.toString();
                props.put("project.version",parentVersion);
            }else if(ARTIFACTID.equals(localName)){
                parentArtifactId = sb.toString();
            }else{
                parentGroupId=sb.toString();
                props.put("project.groupId",parentGroupId);
            }
            sb.setLength(0);
            startReadText = false;
        }

        if(readProperties && startReadText){
            props.put(tmpProName,sb.toString());
            sb.setLength(0);
            startReadText = false;
        }
        if (current != null || root != null) {
            if (localName.equals(DEPENDENCY)) {
                if(!"test".equalsIgnoreCase(current.scope)) {
                    dependencies.add(current);
                }
                current = null;
            } else if (localName.equals(PROJECT)) {
                root.dependencies = dependencies;
            } else {
                if (current != null) {
                    String txt = sb.toString().trim();
                    if(txt.startsWith("${")&&txt.endsWith("}")){
                        txt = props.get(txt.substring(2,txt.length()-1));
                    }
                    switch (localName) {
                        case ARTIFACTID:
                            current.artifactId = txt;
                            break;
                        case VERSION:
                            current.version = txt;
                            break;
                        case SCOPE:
                            current.scope = txt;
                            break;
                        case GROUPID:
                            current.groupId = txt;
                            break;
                    }
                    sb.setLength(0);
                    startReadText = false;
                }

                if (root != null && openElement < 3 && valueElements.contains(localName)) {
                    switch (localName) {
                        case ARTIFACTID:
                            root.artifactId = sb.toString();
                            props.put("project.artifactId",root.artifactId);
                            break;
                        case VERSION:
                            root.version = sb.toString();
                            props.put("project.version",root.version);
                            break;
                        case GROUPID:
                            root.groupId = sb.toString();
                            props.put("project.groupId",root.groupId);
                            break;
                    }
                    sb.setLength(0);
                    startReadText = false;
                }

            }
            openElement--;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        if((root != null && openElement < 3 || current!=null || readParent||readProperties) && startReadText){
            sb.append(ch,start,length);
        }

    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {

    }

    @Override
    public void skippedEntity(String name) throws SAXException {

    }
}
