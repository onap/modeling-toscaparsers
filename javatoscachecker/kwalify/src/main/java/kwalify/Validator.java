/*
 * The original version of this file was provided under the MIT license,
 * as follows:
 *
 * @(#)Validator.java	$Rev: 3 $ $Release: 0.5.1 $
 *
 * Copyright (c) <2005> <kuwata lab>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Modifications to this file are provided under the Apache 2.0 license,
 * as follows:

 * Copyright (c) 2017 <AT&T>.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations 
 * under the License.
 */

package kwalify;

import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
 *  validation engine
 *
 *  ex.
 *  <pre>
 *
 *    // load YAML document
 *    String str = Util.readFile("document.yaml");
 *    YamlParser parser = new YamlParser(str);
 *    Object document = parser.parse();
 *
 *    // load schema
 *    Object schema = YamlUtil.loadFile("schema.yaml");
 *
 *    // generate validator and validate document
 *    Validator validator = new Validator(shema);
 *    List errors = validator.validate(document);
 *
 *    // show errors
 *    if (errors != null && errors.size() > 0) {
 *        parser.setErrorsLineNumber(errors);
 *        java.util.Collections.sort(errors);
 *        for (Iterator it = errors.iterator(); it.hasNext(); ) {
 *            ValidationError error = (ValidationError)it.next();
 *            int linenum = error.getLineNumber();
 *            String path = error.getPath();
 *            String mesg = error.getMessage();
 *            String s = "- (" + linenum + ") [" + path + "] " + mesg;
 *            System.err.println(s);
 *        }
 *    }
 *  </pre>
 *
 *  @version   $Rev: 3 $
 *  @release   $Release: 0.5.1 $
 */
public class Validator {
    private Rule rule;

    public Validator(Map schema) {
        rule = new Rule(schema);
    }

    public Validator(Object schema) {
        rule = new Rule(schema);
    }

    public Rule getRule() {
        return rule;
    }

    public List validate(Object value) {
        ValidationContext vctx = new ValidationContext();
        validateRule(value, rule, vctx);
        return vctx.getErrors();
    }

    protected boolean preValidationHook(Object value, Rule rule, ValidationContext context) {
        // nothing
        return false;
    }

    protected void postValidationHook(Object value, Rule rule, ValidationContext context) {
    }

    private void validateRule(Object value, Rule rule, ValidationContext context) {
        //why is done necessary? why would one end up having to validate twice the same collection??
        if (Types.isCollection(value)) {
            if (context.done(value))
              return;
        }
        if (rule.isRequired() && value == null) {
            Object[] args = new Object[] { Types.typeName(rule.getType()) };
            context.addError("required.novalue", rule, value, args);
            return;
        }

        if (preValidationHook(value, rule, context)) {
          /* a 'higher power says is ok */
          postValidationHook(value, rule, context);
          return;
        }

        //Class klass = rule.getTypeClass();
        //if (klass != null && value != null && !klass.isInstance(value)) {

        int n = context.errorCount();
        defaultValidateRule(value, rule, context);
        if (context.errorCount() != n) {
            return;
        }
        //
        postValidationHook(value, rule, context);
    }

    /* this is the default validation process */
    protected void defaultValidateRule(Object value, Rule rule, ValidationContext context) {

      if (value != null && ! Types.isCorrectType(value, rule.getType())) {
          Object[] args = new Object[] { Types.typeName(rule.getType()) };
          context.addError("type.unmatch", rule, value, args);
          return;
      }
      //
      if (rule.getSequence() != null) {
          assert value == null || value instanceof List;
          validateSequence((List)value, rule, context);
      } else if (rule.getMapping() != null) {
          assert value == null || value instanceof Map;
          validateMapping((Map)value, rule, context);
			} else if (rule.getReference() != null) {
					validateReference(value, rule, context);
      } else {
          validateScalar(value, rule, context);
      }
    }

    private void validateScalar(Object value, Rule rule, ValidationContext context) {
        assert rule.getSequence() == null;
        assert rule.getMapping() == null;
        if (rule.getAssert() != null) {
            //boolean result = evaluate(rule.getAssert());
            //if (! result) {
            //    errors.add("asset.failed", rule, path, value, new Object[] { rule.getAssert() });
            //}
        }
        if (rule.getEnum() != null && !rule.getEnum().contains(value)) {
                //if (Util.matches(keyname, "\\A\\d+\\z") keyname = "enum";
                context.addError("enum.notexist", rule, value, new Object[] { context.getPathElement() });
        }
        //
        if (value == null) {
            return;
        }
        //
        if (rule.getPattern() != null && ! Util.matches(value.toString(), rule.getPatternRegexp())) {
            context.addError("pattern.unmatch", rule, value, new Object[] { rule.getPattern() });
        }
        if (rule.getRange() != null) {
            assert Types.isScalar(value);
            Map range = rule.getRange();
            Object v;
            if ((v = range.get("max")) != null && Util.compareValues(v, value) < 0) {
                context.addError("range.toolarge", rule, value, new Object[] { v.toString() });
            }
            if ((v = range.get("min")) != null && Util.compareValues(v, value) > 0) {
                context.addError("range.toosmall", rule, value, new Object[] { v.toString() });
            }
            if ((v = range.get("max-ex")) != null && Util.compareValues(v, value) <= 0) {
                context.addError("range.toolargeex", rule, value, new Object[] { v.toString() });
            }
            if ((v = range.get("min-ex")) != null && Util.compareValues(v, value) >= 0) {
                context.addError("range.toosmallex", rule, value, new Object[] { v.toString() });
            }
        }
        if (rule.getLength() != null) {
            assert value instanceof String;
            Map length = rule.getLength();
            int len = value.toString().length();
            Integer v;
            if ((v = (Integer)length.get("max")) != null && v.intValue() < len) {
                context.addError("length.toolong", rule, value, new Object[] { Integer.valueOf(len), v });
            }
            if ((v = (Integer)length.get("min")) != null && v.intValue() > len) {
                context.addError("length.tooshort", rule, value, new Object[] { Integer.valueOf(len), v });
            }
            if ((v = (Integer)length.get("max-ex")) != null && v.intValue() <= len) {
                context.addError("length.toolongex", rule, value, new Object[] { Integer.valueOf(len), v });
            }
            if ((v = (Integer)length.get("min-ex")) != null && v.intValue() >= len) {
                context.addError("length.tooshortex", rule, value, new Object[] { Integer.valueOf(len), v });
            }
        }
    }


    private void validateSequence(List sequence, Rule seqRule, ValidationContext context) {
        assert seqRule.getSequence().size() == 1;
        if (sequence == null) {
            return;
        }
        Rule rule = (Rule)seqRule.getSequence().get(0);
        int i = 0;
        for (Iterator it = sequence.iterator(); it.hasNext(); i++) {
            Object val = it.next();
            context.addPathElement(String.valueOf(i));
            validateRule(val, rule, context);  // validate recursively
            context.removePathElement();
        }
        if (rule.getType().equals("map")) {
            Map mapping = rule.getMapping();
            List uniqueKeys = new ArrayList();
            for (Iterator it = mapping.keySet().iterator(); it.hasNext(); ) {
                Object key = it.next();
                Rule mapRule = (Rule)mapping.get(key);
                if (mapRule.isUnique() || mapRule.isIdent()) {
                    uniqueKeys.add(key);
                }
            }
            //
            if (uniqueKeys.size() > 0) {
                for (Iterator it = uniqueKeys.iterator(); it.hasNext(); ) {
                    Object key = it.next();
                    Map table = new HashMap();  // val => index
                    int j = 0;
                    for (Iterator it2 = sequence.iterator(); it2.hasNext(); j++) {
                        Map map = (Map)it2.next();
                        Object val = map.get(key);
                        if (val == null) {
                            continue;
                        }
                        if (table.containsKey(val)) {
                            String path = context.getPath();
                            String prevPath = path + "/" + table.get(val) + "/" + key;
                            context.addPathElement(String.valueOf(j))
                                   .addPathElement(key.toString());
                            context.addError("value.notunique", rule, val, new Object[] { prevPath });
                            context.removePathElement()
                                   .removePathElement();
                        } else {
                            table.put(val, new Integer(j));
                        }
                    }
                }
            }
        } else if (rule.isUnique()) {
            Map table = new HashMap();  // val => index
            int j = 0;
            for (Iterator it = sequence.iterator(); it.hasNext(); j++) {
                Object val = it.next();
                if (val == null) {
                    continue;
                }
                if (table.containsKey(val)) {
                    String path = context.getPath();
                    String prevPath = path + "/" + table.get(val);
                    context.addPathElement(String.valueOf(j))
                           .addError("value.notunique", rule, val, new Object[] { prevPath })
                           .removePathElement();
                } else {
                    table.put(val, new Integer(j));
                }
            }
        }
    }


    private void validateMapping(Map mapping, Rule mapRule, ValidationContext context) {
        if (mapping == null) {
            return;
        }
        Map m = mapRule.getMapping();
        for (Iterator it = m.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Rule rule = (Rule)m.get(key);
            if (rule.isRequired() && !mapping.containsKey(key)) {
                context.addError("required.nokey", rule, mapping, new Object[] { key });
            }
        }
        for (Iterator it = mapping.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Object val = mapping.get(key);
            Rule rule = (Rule)m.get(key);
            context.addPathElement(key.toString());
            if (rule == null) {
                context.addError("key.undefined", rule, mapping, new Object[] { key.toString() + ":", mapRule.getName() + m.keySet().toString() });
            } else {
                validateRule(val, rule, context);  // validate recursively
            }
            context.removePathElement();
        }
    }

    private void validateReference(Object value, Rule refRule, ValidationContext context) {
			//look only up the rule chain. This is a limitation
			Rule refed = refRule;
			while ((refed = refed.getParent()) != null) {
				if (refed.getName() != null && refed.getName().equals(refRule.getReference())) {
					defaultValidateRule(value, refed, context);
					return;
				}
			}
			context.addError("ref.nosuchrule", refRule, value, new Object[] { refRule.getReference() });
		}

    public class ValidationContext {

       private StringBuilder  path = new StringBuilder("");
       private List           errors = new LinkedList();
       private Map	      done = new IdentityHashMap(); //completion tracker

       private ValidationContext() {
       }

       public String getPath() {
         return this.path.toString();
       }

       public Validator getValidator() {
	 return Validator.this;
       }

       public ValidationContext addPathElement(String theElement) {
         this.path.append("/")
                  .append(theElement);
         return this;
       }

       public String getPathElement() {
         int index = this.path.lastIndexOf("/");
         return index >= 0 ? this.path.substring(index + 1) : this.path.toString();
       }

       public ValidationContext removePathElement() {
         int index = this.path.lastIndexOf("/");
         if (index >= 0)
           this.path.delete(index, this.path.length());
         return this;
       }

       protected ValidationContext addError(String errorSymbol, Rule rule, Object value, Object[] args) {
         addError(
	   new ValidationException(
             Messages.buildMessage(errorSymbol, value, args), getPath(), value, rule, errorSymbol));
         return this;
       }

       protected ValidationContext addError(String errorSymbol, Rule rule, String relpath, Object value, Object[] args) {
         addError(
	   new ValidationException(
             Messages.buildMessage(errorSymbol, value, args), getPath()+"/"+relpath, value, rule, errorSymbol));
         return this;
       }

       public ValidationContext addError(String message, Rule rule, Object value, Throwable cause) {
         addError(
	   new ValidationException(
             message + ((cause == null) ? "" : ", cause " + cause), getPath(), value, rule, ""));
         return this;
       }

       public ValidationContext addError(ValidationException theError) {
         this.errors.add(theError);
         return this;
       }


       public List getErrors() {
         return Collections.unmodifiableList(this.errors);
       }

       public boolean hasErrors() {
         return this.errors.isEmpty();
       }

       public int errorCount() {
         return this.errors.size();
       }

       private boolean done(Object theTarget) {
          if (this.done.get(theTarget) != null) {
                return true;
          }
          this.done.put(theTarget, Boolean.TRUE);
          return false;
       }

       private boolean isDone(Object theTarget) {
          return this.done.get(theTarget) != null;
       } 
    }

/*
    public static void main(String[] args) throws Exception {
        Map schema = (Map)YamlUtil.loadFile("schema.yaml");
        Validator validator = new Validator(schema);
        String filename = args.length > 0 ? args[0] : "document.yaml";
        Object document = YamlUtil.loadFile(filename);
        List errors = validator.validate(document);
        if (errors != null && errors.size() > 0) {
            for (Iterator it = errors.iterator(); it.hasNext(); ) {
                ValidationException error = (ValidationException)it.next();
                //String s = "- [" + error.getPath() + "] " + error.getMessage();
                String s = "- <" + error.getErrorSymbol() + ">[" + error.getPath() + "] " + error.getMessage();
                System.out.println(s);
            }
        } else {
            System.out.println("validtion OK.");
        }
    }
*/

}
