package org.yidan.idea.plugin.jasmine;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kongxiangxin on 2017/7/31.
 */
public class GenerateAction extends AnAction implements Logger {

    private boolean generating = false;

    private ProgressIndicator progressIndicator;

    private PsiElement getCurrentPsiElement(AnActionEvent event){
        if(event.getProject() == null){
            return null;
        }
        Object navigatable = event.getData(CommonDataKeys.NAVIGATABLE);
        if(!(navigatable instanceof PsiElement)){
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

        generate(event.getProject(), configNode);

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

    public void error(String message){
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

    private void generate(Project project, VirtualFile configNode){
        if(configNode == null || project == null){
            return;
        }
        if(generating){
            showMessage("Another task is executing, just wait...");
        }
        generating = true;

		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Jasmine"){
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
            	GenerateAction.this.progressIndicator = progressIndicator;
				Generator generator = new Generator(GenerateAction.this);
				generator.generate(configNode.getPath());
            }});
    }

    @Override
    public void info(String message) {
        showMessage(message);
    }

	@Override
	public void error(Exception e) {
		String message = ExceptionUtils.getFullStackTrace(e);
		error(message);
	}

	@Override
	public void setProgress(double percent) {
    	if(progressIndicator != null){
			progressIndicator.setFraction(percent);
			if(percent == 1){
				progressIndicator.setText("Generated");
			}else{
				progressIndicator.setText((int)percent * 100 + "% has generated...");
			}
		}
	}
}
