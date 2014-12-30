package nl.armatiek.xslweb.saxon.functions.expath.file;

import java.io.File;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ZeroOrMore;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import nl.armatiek.xslweb.configuration.Definitions;
import nl.armatiek.xslweb.saxon.functions.expath.file.error.FileException;

import org.apache.commons.io.FileUtils;

public class ReadTextLines extends ExtensionFunctionDefinition {

  private static final StructuredQName qName = new StructuredQName("", Definitions.NAMESPACEURI_EXPATH_FILE, "read-text-lines");

  @Override
  public StructuredQName getFunctionQName() {
    return qName;
  }

  @Override
  public int getMinimumNumberOfArguments() {
    return 1;
  }

  @Override
  public int getMaximumNumberOfArguments() {
    return 2;
  }

  @Override
  public SequenceType[] getArgumentTypes() {    
    return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {    
    return SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE);
  }
  
  @Override
  public boolean hasSideEffects() {    
    return false;
  }

  @Override
  public ExtensionFunctionCall makeCallExpression() {    
    return new ReadTextLinesCall();
  }
  
  private static class ReadTextLinesCall extends FileExtensionFunctionCall {
        
    @Override
    public ZeroOrMore<StringValue> call(XPathContext context, Sequence[] arguments) throws XPathException {      
      try {                        
        File file = getFile(((StringValue) arguments[0].head()).getStringValue());
        if (!file.exists()) {
          throw new FileException(String.format("File \"%s\" does not exist", 
              file.getAbsolutePath()), FileException.ERROR_PATH_NOT_EXIST);
        }
        if (file.isDirectory()) {
          throw new FileException(String.format("Path \"%s\" points to a directory", 
              file.getAbsolutePath()), FileException.ERROR_PATH_IS_DIRECTORY);
        }         
        String encoding = "UTF-8";
        if (arguments.length > 1) {
          encoding = ((StringValue) arguments[1].head()).getStringValue();                   
        }        
        Iterator<String> linesIter;
        try {
          linesIter = FileUtils.readLines(file, encoding).iterator();
        } catch (UnsupportedCharsetException ece) {
          throw new FileException(String.format("Encoding \"%s\" is invalid or not supported", 
              encoding), FileException.ERROR_UNKNOWN_ENCODING);
        }                            
        ArrayList<StringValue> lines = new ArrayList<StringValue>();                                      
        while (linesIter.hasNext()) {                                  
          lines.add(new StringValue(linesIter.next()));                    
        }                        
        return new ZeroOrMore<StringValue>(lines.toArray(new StringValue[lines.size()]));
      } catch (Exception e) {
        throw new FileException("Other file error", e, FileException.ERROR_IO);
      }
    } 
  }
}