package org.jetbrains.plugins.ideavim.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.maddyhome.idea.vim.KeyHandler;
import com.maddyhome.idea.vim.helper.EditorDataContext;
import com.maddyhome.idea.vim.helper.RunnableHelper;
import org.jetbrains.plugins.ideavim.VimTestCase;

import javax.swing.*;
import java.util.List;

import static com.maddyhome.idea.vim.helper.StringHelper.stringToKeys;

/**
 * @author vlan
 */
public class ChangeActionTest extends VimTestCase {
  // |c| |t|
  public void testChangeLinesTillForwards() {
    doTest(stringToKeys("ct(for "),
           "<caret>if (condition) {\n" +
           "}\n",
           "for (condition) {\n" +
           "}\n");
  }

  // VIM-276 |c| |T|
  public void testChangeLinesTillBackwards() {
    doTest(stringToKeys("cT("),
           "if (condition) {<caret>\n" +
           "}\n",
           "if (\n" +
           "}\n");
  }

  // VIM-276 |c| |F|
  public void testChangeLinesToBackwards() {
    doTest(stringToKeys("cFc"),
           "if (condition) {<caret>\n" +
           "}\n",
           "if (\n" +
           "}\n");
  }

  // VIM-311 |i_CTRL-O|
  public void testInsertSingleCommand() {
    final List<KeyStroke> keys = stringToKeys("idef");
    keys.add(KeyStroke.getKeyStroke("control O"));
    keys.addAll(stringToKeys("d2hx"));
    doTest(keys,
           "abc<caret>.\n",
           "abcdx.\n");
  }

  // VIM-321 |d| |count|
  public void testDeleteEmptyRange() {
    doTest(stringToKeys("d0"),
           "<caret>hello\n",
           "hello\n");
  }

  // VIM-112 |i| |i_CTRL-W|
  public void testInsertDeletePreviousWord() {
    final List<KeyStroke> keys = stringToKeys("ione two three");
    keys.add(KeyStroke.getKeyStroke("control W"));
    typeTextInFile(keys,
                   "hello\n" +
                   "<caret>\n");
    myFixture.checkResult("hello\n" +
                          "one two \n");
  }

  // VIM-157 |~|
  public void testToggleCharCase() {
    doTest(stringToKeys("~~"),
           "<caret>hello world\n",
           "HEllo world\n");
  }

  // VIM-157 |~|
  public void testToggleCharCaseLineEnd() {
    doTest(stringToKeys("~~"),
           "hello wor<caret>ld\n",
           "hello worLD\n");
  }

  // VIM-85 |i| |gi| |gg|
  public void testInsertAtPreviousAction() {
    final List<KeyStroke> keys = stringToKeys("ihello");
    keys.add(KeyStroke.getKeyStroke("ESCAPE"));
    keys.addAll(stringToKeys("gggi world! "));
    doTest(keys,
           "one\n" +
           "two <caret>three\n" +
           "four\n",
           "one\n" +
           "two hello world! three\n" +
           "four\n");
  }

  // VIM-312 |d| |w|
  public void testDeleteLastWordInFile() {
    doTest(stringToKeys("dw"),
           "<caret>hello\n",
           "\n");
  }

  // |d| |w|
  public void testDeleteLastWordBeforeEOL() {
    doTest(stringToKeys("dw"),
           "one <caret>two\n" +
           "three\n",
           "one \n" +
           "three\n");
  }

  // VIM-105 |d| |w|
  public void testDeleteLastWordBeforeEOLs() {
    doTest(stringToKeys("dw"),
           "one <caret>two\n" +
           "\n" +
           "three\n",
           "one \n" +
           "\n" +
           "three\n");
  }

  // VIM-105 |d| |w|
  public void testDeleteLastWordBeforeEOLAndWhitespace() {
    doTest(stringToKeys("dw"),
           "one <caret>two\n" +
           " three\n",
           "one \n" +
           " three\n");
  }

  // VIM-200 |c| |w|
  public void testChangeWordAtLastChar() {
    doTest(stringToKeys("cw"),
           "on<caret>e two three\n",
           "on two three\n");
  }

  // VIM-300 |c| |w|
  public void testChangeWordTwoWordsWithoutWhitespace() {
    doTest(stringToKeys("cw"),
           "<caret>$value\n",
           "value\n");
  }

  private void doTest(final List<KeyStroke> keys, String before, String after) {
    myFixture.configureByText("a.java", before);
    final Editor editor = myFixture.getEditor();
    final KeyHandler keyHandler = KeyHandler.getInstance();
    final EditorDataContext dataContext = new EditorDataContext(editor);
    final Project project = myFixture.getProject();
    RunnableHelper.runWriteCommand(project, new Runnable() {
      @Override
      public void run() {
        for (KeyStroke key : keys) {
          keyHandler.handleKey(editor, key, dataContext);
        }
      }
    }, null, null);
    myFixture.checkResult(after);
  }
}
