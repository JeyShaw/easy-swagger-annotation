package com.easyswagger.shaw.processor;

import com.easyswagger.shaw.util.TreeMarkerUtil;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.lang.reflect.Method;

/**
 * @author JShaw
 */
public abstract class MyAbstractProcessor extends AbstractProcessor {

    protected Trees trees;

    protected TreeMaker treeMaker;

    protected Name.Table names;

    protected Messager messager;

    protected Elements elementUtils;

    protected TreeMarkerUtil treeMarkerUtil;

    /**
     * 初始化，获取编译环境
     *
     * @param env
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        ProcessingEnvironment environment = jbUnwrap(ProcessingEnvironment.class, env);
        super.init(environment);
        trees = Trees.instance(environment);
        Context context = ((JavacProcessingEnvironment) environment).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context).table;
        messager = env.getMessager();
        elementUtils = env.getElementUtils();
        treeMarkerUtil = new TreeMarkerUtil(treeMaker, names);
    }

    protected JCTree.JCCompilationUnit getCompilationTree(Element element) {
        // Assert.check(!element.getKind().equals(Tree.Kind.CLASS));
        // 处理引入类
        TreePath treePath = trees.getPath(element);
        return (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
    }

    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        }
        catch (Throwable ignored) {}
        return unwrapped != null? unwrapped : wrapper;
    }

}
