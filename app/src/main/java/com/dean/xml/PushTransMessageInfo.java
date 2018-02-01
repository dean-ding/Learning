package com.dean.xml;

import com.google.gson.Gson;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created: tvt on 18/1/11 09:49
 */
public class PushTransMessageInfo extends DefaultHandler
{
    //<Requests><Request><MsgType>SendTPNSTransPushMsg</MsgType><MsgID>b8c6847459b06b44892651d961e6a8ba</MsgID><Params><PushMsg><PushMsgType>ADMsg</PushMsgType><PushMsgSubType
    // /><TokenInfo><AppIDStr>AND_M_PH_SuperLivePlus</AppIDStr><AppUserToken>FPC_000000000000000000000000000000000000000000000000000000000001</AppUserToken></TokenInfo><NotifyInfo
    // ><Title>testTitle</Title><Msg>testMsg</Msg></NotifyInfo><MsgContent><![CDATA[{"id":1,"url":"http://ad.html"}]]></MsgContent></PushMsg></Params></Request></Requests>'

    private String tagName = null;
    public String MsgID = "";
    public String PushMsgType = "";
    public String PushMsgSubType = "";
    public String AppIDStr = "";
    public String AppUserToken = "";
    public String Title = "";
    public String Msg = "";
    public ADContent mMsgContent;

    public static class ADContent
    {
        private int id;
        private String url;

        public int getId()
        {
            return id;
        }

        public String getUrl()
        {
            return url;
        }

    }

    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException
    {
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        super.startElement(uri, localName, qName, attributes);
        this.tagName = localName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        super.endElement(uri, localName, qName);
        this.tagName = null;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        super.characters(ch, start, length);
        if (tagName != null)
        {
            String data = new String(ch, start, length);
            if (tagName.equals("MsgID"))
            {
                MsgID = data;
            }
            else if (tagName.equals("PushMsgType"))
            {
                PushMsgType = data;
            }
            else if (tagName.equals("PushMsgSubType"))
            {
                PushMsgSubType = data;
            }
            else if (tagName.equals("AppIDStr"))
            {
                AppIDStr = data;
            }
            else if (tagName.equals("AppUserToken"))
            {
                AppUserToken = data;
            }
            else if (tagName.equals("Title"))
            {
                Title = data;
            }
            else if (tagName.equals("Msg"))
            {
                Msg = data;
            }
            else if (tagName.equals("MsgContent"))
            {
                if (PushMsgType.equals("ADMsg"))
                {
                    mMsgContent = new Gson().fromJson(data, ADContent.class);
                }
            }
        }
    }
}
