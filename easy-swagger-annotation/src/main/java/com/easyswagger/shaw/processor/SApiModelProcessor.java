package com.easyswagger.shaw.processor;

import com.easyswagger.shaw.annotation.SApiModel;
import com.easyswagger.shaw.consts.SwaggerImportEnum;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author JShaw
 */
@SupportedAnnotationTypes("com.easyswagger.shaw.annotation.SApiModel")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SApiModelProcessor extends MyAbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SApiModel.class)) {
            switch(element.getKind()) {
                case CLASS:
                    messager.printMessage(Diagnostic.Kind.WARNING, "开始填充类注解: " + element.getSimpleName());
                    // 处理引入类
                    JCTree.JCCompilationUnit compilationTree = this.getCompilationTree(element);
                    messager.printMessage(Diagnostic.Kind.NOTE, "添加import开始");
                    addImport(element, compilationTree);
                    messager.printMessage(Diagnostic.Kind.NOTE, "添加类注解开始");
                    addClassAnnotation(element, compilationTree);
                    messager.printMessage(Diagnostic.Kind.NOTE, "添加字段注解开始");
                    addFiledAnnotation(element, compilationTree);
                    break;
                default: messager.printMessage(Diagnostic.Kind.WARNING, "暂不支持的织入类型点: " + element.getKind());
            }
        }
        return true;
    }

    private void addFiledAnnotation(Element element, JCTree.JCCompilationUnit compilationTree) {
        JCTree tree = (JCTree) trees.getTree(element);
        tree.accept(new TreeTranslator() {

            @Override
            public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                try {
                    JCTree varTree = jcVariableDecl.getTree();
                    TreePath varTreePath = trees.getPath(compilationTree, varTree);
                    String docComment = treeMarkerUtil.getDocDesc(trees.getDocComment(varTreePath));
                    JCTree.JCAnnotation anno = treeMarkerUtil.createAnnotation(SwaggerImportEnum.ApiModelProperty.getFullName(), docComment);
                    jcVariableDecl.mods.annotations = jcVariableDecl.mods.annotations.append(anno);
                    super.visitVarDef(jcVariableDecl);
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "向变量\"" + jcVariableDecl + "\"添加文档注释发生了错误！");
                    throw e;
                }
            }
        });
    }

    private void addClassAnnotation(Element element, JCTree.JCCompilationUnit compilationTree) {
        JCTree tree = (JCTree) trees.getTree(element);
        tree.accept(new TreeTranslator() {
            @Override
            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                String docComment = trees.getDocComment(trees.getPath(compilationTree, jcClassDecl));
                docComment = treeMarkerUtil.getDocDesc(docComment);
                JCTree.JCAnnotation anno = treeMarkerUtil.createAnnotation(SwaggerImportEnum.ApiModel.getFullName(), docComment);
                jcClassDecl.mods.annotations=jcClassDecl.mods.annotations.append(anno);
                super.visitClassDef(jcClassDecl);
            }
        });
    }

    private void addImport(Element element, JCTree.JCCompilationUnit compilationTree) {
        java.util.List<JCTree> trees = new ArrayList<>();
        trees.addAll(compilationTree.defs);
        java.util.List<JCTree> existImports = new ArrayList<>();
        trees.forEach(e->{
            if(e.getKind().equals(Tree.Kind.IMPORT)){
                existImports.add(e);
            }
        });

        java.util.List<JCTree.JCImport> newImports = buildImportList();
        for (JCTree.JCImport newImport : newImports) {
            if (!checkImportExist(existImports, newImport)) {
                existImports.add(newImport);
                trees.add(0, newImport);
            }
        }
        compilationTree.defs=List.from(trees);
    }

    private java.util.List<JCTree.JCImport> buildImportList() {
        java.util.List<JCTree.JCImport> importList =new ArrayList<>();
        importList.add(treeMarkerUtil.createImport(SwaggerImportEnum.ApiModel.getPkg(),
                SwaggerImportEnum.ApiModel.getClazz(), false));
        importList.add(treeMarkerUtil.createImport(SwaggerImportEnum.ApiModelProperty.getPkg(),
                SwaggerImportEnum.ApiModelProperty.getClazz(), false));
        return importList;
    }

    private boolean checkImportExist(java.util.List<JCTree> existImport, JCTree.JCImport newImport) {
        boolean exist = false;
        for (int i = 0; i < existImport.size(); i++) {
            if(existImport.get(i).toString().equals(newImport.toString())){
                exist = true;
            }
        }
        return exist;
    }

}
