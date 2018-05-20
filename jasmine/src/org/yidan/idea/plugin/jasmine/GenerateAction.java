package org.yidan.idea.plugin.jasmine;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.yidan.idea.plugin.jasmine.dao.MetaDataDao;
import org.yidan.idea.plugin.jasmine.meta.Database;
import org.yidan.idea.plugin.jasmine.settings.GenerateSetting;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by kongxiangxin on 2017/7/31.
 */
public class GenerateAction extends AnAction implements Logger {

    private boolean generating = false;

    private PsiElement getCurrentPsiElement(AnActionEvent event){
        if(event.getProject() == null){
            return null;
        }
        Object navigatable = event.getData(CommonDataKeys.NAVIGATABLE);
        if(navigatable == null || !(navigatable instanceof PsiElement)){
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            if(editor == null){
                return null;
            }
            navigatable = PsiDocumentManager.getInstance(event.getProject()).getPsiFile(editor.getDocument());
            if(navigatable == null){
                return null;
            }
        }
        return (PsiElement)navigatable;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        PsiElement psiElement = getCurrentPsiElement(event);
        if(psiElement == null){
            showMessage("请选择module");
            return;
        }

        Module module = findModule(psiElement);
        if(module == null || module.getModuleFile() == null){
            return;
        }

        VirtualFile root = module.getModuleFile().getParent();

        if(root == null){
            return;
        }
        VirtualFile configNode = root.findChild("jasmine.property");

        if(configNode == null){
            return;
        }

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(configNode.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
            alert(e.getMessage());
            return;
        }

        GenerateSetting setting = GenerateSetting.getInstance(prop);

        generate(event.getProject(), root, setting);

//        showMessage(prop.getProperty("name"));
//
//        VirtualFile test = root.findFileByRelativePath("src/main/java");
//        PsiDirectory dir = PsiManager.getInstance(event.getProject()).findDirectory(test);
//
//        dir.createFile("tttt.html");
//        PsiFileFactory.getInstance(event.getProject()).createFileFromText()

    }

    private Module findModule(PsiElement element){
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if(module != null){
            return module;
        }
        return findModule(element.getParent());
    }

    private void alert(Throwable e){
        String message = ExceptionUtils.getFullStackTrace(e);
        alert(message);
    }

    private void alert(String message){
        if(message == null){
            return;
        }
        Notification notification = new Notification("jasmine", "jasmine", message, NotificationType.ERROR);
        Notifications.Bus.notify(notification);

//        Runnable runnable = () -> Messages.showDialog(message, "jasmine", new String[]{"OK"}, -1, JasmineIcons.Jasmine);
//
//        Application application = ApplicationManager.getApplication();
//        if (application.isDispatchThread()) {
//            runnable.run();
//        } else {
//            application.invokeLater(runnable);
//        }
    }

    private void showMessage(String message){
        if(message == null){
            return;
        }
        Notification notification = new Notification("jasmine", "jasmine", message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }
    @Override
    public void update(AnActionEvent anActionEvent) {
        PsiElement element = getCurrentPsiElement(anActionEvent);
        anActionEvent.getPresentation().setEnabledAndVisible(element != null);
    }

    private List<VirtualFile> findTemplateEntry(Project project, VirtualFile moduleRoot){
        List<VirtualFile> entries = new ArrayList<>();
        VfsUtilCore.visitChildrenRecursively(moduleRoot, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if(file.getName().toLowerCase().endsWith(".jm.vm")){
                    entries.add(file);
                }
                return super.visitFile(file);
            }
        });
//        VirtualFile[] children = moduleRoot.getChildren();
//        for(VirtualFile file : children){
//            if(file.getName().toLowerCase().endsWith(".jm.vm")){
//                entries.add(file);
//            }
//            if(file instanceof VirtualDirectoryImpl){
//                List<VirtualFile> subEntries = findTemplateEntry(project, file);
//                entries.addAll(subEntries);
//            }
//        }
        return entries;

//        return FilenameIndex.getAllFilesByExt(project, "jm-entry", GlobalSearchScope.moduleScope(ModuleUtilCore.findModuleForFile(moduleRoot, project)));
    }

    private void generate(Project project, VirtualFile moduleRoot, GenerateSetting setting){
        if(moduleRoot == null || project == null){
            return;
        }
        if(setting == null){
            alert("未找到配置[module:" + moduleRoot.getName() + "]");
            return;
        }
        if(generating){
            showMessage("已经存在一个正在生成的任务了, 请稍后再试...");
        }
        generating = true;
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "jasmine"){
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                showMessage("开始生成" + moduleRoot.getName() + "...");

                // start your process
                try{
                    PsiManager psiManager = PsiManager.getInstance(project);
                    MetaDataDao dao = new MetaDataDao(setting);
                    Database database = dao.getDatabase();

                    List<VirtualFile> entries = findTemplateEntry(project, moduleRoot);
                    if(entries.isEmpty()){
                        alert("未找到模板[module:" + moduleRoot.getName() + "]");
                        return;
                    }
                    // Set the progress bar percentage and text
                    progressIndicator.setFraction(0.10);
                    progressIndicator.setText("开始生成 " + moduleRoot.getName() + "...");

                    int index = 1;
                    for(VirtualFile file : entries){
                        generate(file, database, setting);
                        float percent = index * 1.0f / entries.size();
                        progressIndicator.setFraction(percent);
                        progressIndicator.setText((int)percent * 100 + "% 已生成...");
                        index ++;
                    }

                }catch (Throwable e){
                    e.printStackTrace();
                    alert(e);
                }finally {
                    generating = false;
                    showMessage("生成结束");

                    // Finished
                    progressIndicator.setFraction(1.0);
                    progressIndicator.setText("生成结束");
                }
            }});

    }

    private void generate(VirtualFile templateEntry, Database database, GenerateSetting setting){
        TemplateProcessor processor = new TemplateProcessor(setting, templateEntry, database, this);
        processor.process();;

    }

    @Override
    public void showError(String message) {
        alert(message);
    }

    @Override
    public void showInfo(String message) {
        showMessage(message);
    }
}
