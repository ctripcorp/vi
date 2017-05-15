package com.ctrip.framework.cornerstone.util;


import javax.xml.xpath.XPathExpressionException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiang.j on 2016/5/3.
 */
public final class TextUtils {

    public static String getTagInnerText(final String tagName,final String xml) throws XPathExpressionException {
        Pattern pattern = Pattern.compile("<\\s*("+tagName+")\\s*>([^</\\s*\1\\s*>]*)",Pattern.MULTILINE);//;

        Matcher mather = pattern.matcher(xml);
        if(mather.find()) {
            return String.valueOf(mather.group(2));
        }else{
            return "";
        }

    }

    public static String formatString(final String raw,Map<String,String> params){

        return formatString("%{",'}',raw,params);
    }

    public static String formatString(String startDelimiter,char endDelimiter, final String raw,Map<String,String> params){

        char[] startChar = startDelimiter.toCharArray();
        int judegStartIndex = 0;
        final int maxLen = 50;

        StringBuilder sb = new StringBuilder(raw.length());
        StringBuilder tmp = new StringBuilder();

        for(char c:raw.toCharArray()){

            int tmpLen = tmp.length();

            boolean isStartEnd = judegStartIndex>=startChar.length;

            if((!isStartEnd && c==startChar[judegStartIndex]) ||
                    (isStartEnd && tmpLen>0 && tmpLen<maxLen)){
                judegStartIndex++;
                if(isStartEnd && c==endDelimiter){
                    String key = tmp.substring(startChar.length);
                    if(params.containsKey(key)) {
                        sb.append(params.get(key));
                    }

                    tmp.delete(0,tmp.length());
                    judegStartIndex = 0;
                }else {
                    tmp.append(c);
                }
            }else{
                sb.append(tmp.toString());
                tmp.delete(0,tmp.length());
                judegStartIndex =0;
                sb.append(c);
            }

        }
        return sb.toString();
    }
    public static String join(String join, Iterable<String> sources) {
        if(sources==null){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(String item : sources){
            sb.append(join).append(item);
        }
        return sb.toString().substring(1);
    }
    public static String makeJSErrorMsg(String msg,String type){
        return "$@###@"+msg+"@"+type;
    }

    public static String nullToEmpty(String str){
        if(str==null){
            return "";
        }else{
            return str;
        }
    }
    public static String nullToNA(String str){
        if(str==null || "null".equals(str)){
            return "N/A";
        }else{
            return str;
        }
    }

    public static boolean isEmpty(Object str) {
		return (str == null || "".equals(str));
	}

	/**
	 * Check that the given {@code CharSequence} is neither {@code null} nor
	 * of length 0.
	 * <p>Note: this method returns {@code true} for a {@code CharSequence}
	 * that purely consists of whitespace.
	 * <p><pre class="code">
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * @param str the {@code CharSequence} to check (may be {@code null})
	 * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
	 * @see #hasText(String)
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * Check that the given {@code String} is neither {@code null} nor of length 0.
	 * <p>Note: this method returns {@code true} for a {@code String} that
	 * purely consists of whitespace.
	 * @param str the {@code String} to check (may be {@code null})
	 * @return {@code true} if the {@code String} is not {@code null} and has length
	 * @see #hasLength(CharSequence)
	 * @see #hasText(String)
	 */
	public static boolean hasLength(String str) {
		return hasLength((CharSequence) str);
	}

	/**
	 * Check whether the given {@code CharSequence} contains actual <em>text</em>.
	 * <p>More specifically, this method returns {@code true} if the
	 * {@code CharSequence} is not {@code null}, its length is greater than
	 * 0, and it contains at least one non-whitespace character.
	 * <p><pre class="code">
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * @param str the {@code CharSequence} to check (may be {@code null})
	 * @return {@code true} if the {@code CharSequence} is not {@code null},
	 * its length is greater than 0, and it does not contain whitespace only
	 * @see Character#isWhitespace
	 */
	public static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given {@code String} contains actual <em>text</em>.
	 * <p>More specifically, this method returns {@code true} if the
	 * {@code String} is not {@code null}, its length is greater than 0,
	 * and it contains at least one non-whitespace character.
	 * @param str the {@code String} to check (may be {@code null})
	 * @return {@code true} if the {@code String} is not {@code null}, its
	 * length is greater than 0, and it does not contain whitespace only
	 * @see #hasText(CharSequence)
	 */
	public static boolean hasText(String str) {
		return hasText((CharSequence) str);
	}
}
