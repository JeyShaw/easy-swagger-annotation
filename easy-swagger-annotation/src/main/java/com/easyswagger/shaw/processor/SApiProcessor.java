package com.easyswagger.shaw.processor;

import com.easyswagger.shaw.annotation.SApi;
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
 * 对SApi的注解处理器
 * @author JShaw
 */
@SupportedAnnotationTypes("com.easyswagger.shaw.annotation.SApi")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SApiProcessor extends MyAbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SApi.class)) {
            switch(element.getKind()) {
                case CLASS:
                    messager.printMessage(Diagnostic.Kind.WARNING, "开始填充类注解: " + element.getSimpleName());
                    JCTree.JCCompilationUnit compilationTree = super.getCompilationTree(element);
                    addImport(element, compilationTree);
                    addClassAnnotation(element, compilationTree);
                    addMethodAnnotation(element, compilationTree);
                    break;
                default: messager.printMessage(Diagnostic.Kind.WARNING, "暂不支持的织入类型点: " + element.getKind());
            }
        }
        return true;
    }

    private void addMethodAnnotation(Element element, JCTree.JCCompilationUnit compilationTree) {
        JCTree jcTree = (JCTree) trees.getTree(element);
        jcTree.accept(new TreeTranslator(){

            @Override
            public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                messager.printMessage(Diagnostic.Kind.WARNING, "方法名字为: " + jcMethodDecl.getName());
                if (!"<init>".equals(jcMethodDecl.getName().toString())) {
                    TreePath path = trees.getPath(compilationTree, jcMethodDecl);
                    String docComment = treeMarkerUtil.getDocDesc(trees.getDocComment(path));
                    JCTree.JCAnnotation anno = treeMarkerUtil.createAnnotation(SwaggerImportEnum.ApiOperation.getFullName(), docComment);
                    messager.printMessage(Diagnostic.Kind.WARNING, "生成注解: " + anno.toString());
                    List<JCTree.JCAnnotation> annotations = jcMethodDecl.mods.annotations;
                    boolean exist = false;
                    for (JCTree.JCAnnotation annotation : annotations) {
                        // FIXME 需要比较限定的注解名称，要分成简写和全写的情况，暂时未做处理
                        if (treeMarkerUtil.annoEqual(anno, annotation)) {
                            exist = true;
                            break;
                        }
                    }
                    if (!false) {
                        jcMethodDecl.mods.annotations = jcMethodDecl.mods.annotations.append(anno);
                    }
                }
                super.visitMethodDef(jcMethodDecl);
            }
        });
        // 获取字段的文档注释，创建注解，写入
    }

    private void addClassAnnotation(Element element, JCTree.JCCompilationUnit compilationTree) {
        JCTree jcTree = (JCTree) trees.getTree(element);
        jcTree.accept(new TreeTranslator(){
            @Override
            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            String docComment = trees.getDocComment(trees.getPath(compilationTree, jcClassDecl));
            docComment = treeMarkerUtil.getDocDesc(docComment);
            // List<JCTree> defs = jcClassDecl.defs;
            JCTree.JCAnnotation jcAnnotation = treeMarkerUtil.createAnnotation(SwaggerImportEnum.Api.getFullName(),
                    docComment);
            //在原有类定义中append新的注解对象
            jcClassDecl.mods.annotations=jcClassDecl.mods.annotations.append(jcAnnotation);
            super.visitClassDef(jcClassDecl);
            }

            /*
            @Override
            public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                // TODO 这个声明周期过程是否会走到？
                super.visitVarDef(jcVariableDecl);
                JCTree.JCModifiers modifiers = jcVariableDecl.getModifiers();
                List<JCTree.JCAnnotation> annotations = modifiers.getAnnotations();
                if (annotations == null || annotations.size() <= 0) {
                    return;
                }
                JCTree.JCAnnotation jcAnnotation = treeMarkerUtil.createAnnotation(SwaggerImportEnum.ApiModelProperty.getFullName(),
                        annoValue);
                jcVariableDecl.mods.annotations = jcVariableDecl.mods.annotations.append(jcAnnotation);
                super.visitVarDef(jcVariableDecl);
                /*
                // 以下只是简单实现, 不考虑是否已存在setter方法
                for (JCTree.JCAnnotation annotation : annotations) {
                    if (RewriteEmptyStringAsNull.class.getName().equals(annotation.type.toString())) {
                        // 生成getter方法
                        JCTree.JCMethodDecl setterMethod = createSetterMethod(jcVariableDecl);
                        this.setters = this.setters.append(setterMethod);
                    }
                }
            }
                 */
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
        importList.add(treeMarkerUtil.createImport(SwaggerImportEnum.Api.getPkg(),
                SwaggerImportEnum.Api.getClazz(), false));
        importList.add(treeMarkerUtil.createImport(SwaggerImportEnum.ApiOperation.getPkg(),
                SwaggerImportEnum.ApiOperation.getClazz(), false));
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
