package com.easyswagger.shaw.util;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * @author JShaw
 */
public class TreeMarkerUtil {

    private final TreeMaker treeMaker;
    private final Name.Table names;

    public TreeMarkerUtil(TreeMaker treeMaker, Name.Table names) {
        this.treeMaker = treeMaker;
        this.names = names;
    }

    public JCTree.JCImport createImport(String pkg, String clasName, boolean isStatic) {
        JCTree.JCIdent ident = treeMaker.Ident(names.fromString(pkg));
        return treeMaker.Import(treeMaker.Select(
                ident, names.fromString(clasName)), false);
    }

    /**
     * 生成注解对象
     * @param annoClazz 注解类型
     * @param value 注解值
     * @return 注解对象
     */
    public JCTree.JCAnnotation createAnnotation(String annoClazz, String value) {
        // 1. 处理注解参数 2. 创建注解对象
        JCTree.JCExpression arg = makeArg("value", value);
        return makeAnnotation(annoClazz, List.of(arg));
    }

    public JCTree.JCExpression makeArg(String key, String value){
        //注解需要的参数是表达式，这里的实际实现为等式对象，Ident是值，Literal是value，最后结果为a=b
        JCTree.JCExpression arg = treeMaker.Assign(treeMaker.Ident(names.fromString(key)), treeMaker.Literal(value));
        return arg;
    }

    private JCTree.JCAnnotation makeAnnotation(String annotationName, List<JCTree.JCExpression> args){
        JCTree.JCExpression expression=chainDots(annotationName.split("\\."));
        return treeMaker.Annotation(expression, args);
    }

    public JCTree.JCExpression chainDots(String... elems) {
        assert elems != null;
        JCTree.JCExpression e = null;
        for (int i = 0 ; i < elems.length ; i++) {
            e = e == null ? treeMaker.Ident(names.fromString(elems[i])) : treeMaker.Select(e, names.fromString(elems[i]));
        }
        assert e != null;
        return e;
    }

    public String getDocDesc(String document) {
        // 截取到第一个@ 符号之前
        return document == null
                ? ""
                : document.trim().split("@")[0].trim();
    }

    public boolean annoEqual(JCTree.JCAnnotation anno, JCTree.JCAnnotation annotation) {
        return false;
    }
}
