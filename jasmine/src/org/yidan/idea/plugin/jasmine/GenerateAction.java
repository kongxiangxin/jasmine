package org.yidan.idea.plugin.jasmine;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKeys;
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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.yidan.idea.plugin.jasmine.dao.MetaDataDao;
import org.yidan.idea.plugin.jasmine.meta.Database;
import org.yidan.idea.plugin.jasmine.settings.GenerateSetting;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static groovyjarjarantlr.build.ANTLR.root;

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
            showMessage("Please select a node in the project tool window");
            return;
        }

        VirtualFile currentFile = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        if(currentFile == null){
            return;
        }

        //find the jasmine.properties file node
        VirtualFile configNode = findConfigFile(currentFile);

        if(configNode == null){
            showMessage("Cannot found jasmine.properties in '" + currentFile.getName() + "' node's inheritance chain");
            return;
        }

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(configNode.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
            showError(e.getMessage());
            return;
        }

        GenerateSetting setting = GenerateSetting.getInstance(prop);

        generate(event.getProject(), configNode.getParent(), setting);

    }

    /**
     * find the jasmine.properties file node
     * @param current
     * @return
     */
    private VirtualFile findConfigFile(VirtualFile current){
        if(current != null){
            VirtualFile configFile = current.findChild("jasmine.properties");
            if(configFile != null){
                return configFile;
            }
            return findConfigFile(current.getParent());
        }
        return null;
    }

    private void showError(Throwable e){
        String message = ExceptionUtils.getFullStackTrace(e);
        showError(message);
    }

    public void showError(String message){
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

    /**
     * find template file matches *.jm.vm
     * @param moduleRoot
     * @return
     */
    private List<VirtualFile> findTemplateEntry(VirtualFile moduleRoot){
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
        return entries;
    }

    private void generate(Project project, VirtualFile moduleRoot, GenerateSetting setting){
        if(moduleRoot == null || project == null){
            return;
        }
        if(setting == null){
            showError("Cannot found jasmime.properties[parent:" + moduleRoot.getName() + "]");
            return;
        }
        if(generating){
            showMessage("Another task is executing, just wait...");
        }
        generating = true;
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Jasmine"){
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                showMessage("Generating" + moduleRoot.getName() + "...");

                // start your process
                try{
                    MetaDataDao dao = new MetaDataDao(setting);
                    Database database = dao.getDatabase();

                    List<VirtualFile> entries = findTemplateEntry(moduleRoot);
                    if(entries.isEmpty()){
                        showError("No template found [module:" + moduleRoot.getName() + "]");
                        return;
                    }
                    // Set the progress bar percentage and text
                    progressIndicator.setFraction(0.10);
                    progressIndicator.setText("Generating " + moduleRoot.getName() + "...");

                    int index = 1;
                    for(VirtualFile file : entries){
                        generate(file, database, setting);
                        float percent = index * 1.0f / entries.size();
                        progressIndicator.setFraction(percent);
                        progressIndicator.setText((int)percent * 100 + "% has generated...");
                        index ++;
                    }

                }catch (Throwable e){
                    e.printStackTrace();
                    showError(e);
                }finally {
                    generating = false;
                    showMessage("Generated");

                    // Finished
                    progressIndicator.setFraction(1.0);
                    progressIndicator.setText("Generated");
                }
            }});

    }

    private void generate(VirtualFile templateEntry, Database database, GenerateSetting setting){
        TemplateProcessor processor = new TemplateProcessor(setting, templateEntry, database, this);
        processor.process();;

    }

    @Override
    public void showInfo(String message) {
        showMessage(message);
    }
}
