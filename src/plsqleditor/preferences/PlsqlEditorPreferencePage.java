/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package plsqleditor.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlConfiguration;
import plsqleditor.editors.PlSqlPartitionScanner;

/*
 * The page for setting the editor options.
 */
@SuppressWarnings("restriction")
public class PlsqlEditorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage
{

	private static final String BOLD = PreferenceConstants.EDITOR_BOLD_SUFFIX;

	public final OverlayPreferenceStore.OverlayKey[] fKeys = new OverlayPreferenceStore.OverlayKey[] {

			// -------------------------------------
			// Appearance Tab
			// -------------------------------------
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT,
					PreferenceConstants.P_EDITOR_TAB_WIDTH),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.EDITOR_LINE_WRAP),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.SOURCE_FOLDING),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.AUTO_COMPLETION_QUOTE1),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.AUTO_COMPLETION_QUOTE2),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.AUTO_COMPLETION_BRACKET1),
			// new
			// OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
			// PreferenceConstants.AUTO_COMPLETION_BRACKET2),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.AUTO_COMPLETION_BRACKET3),
			// new
			// OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
			// PreferenceConstants.AUTO_COMPLETION_BRACKET4),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.EDITOR_SMART_HOME_END),
			// new
			// OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
			// PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION),
			// -------------------------------------

			// -------------------------------------
			// Syntax Tab
			// -------------------------------------
//			new OverlayPreferenceStore.OverlayKey(
//					OverlayPreferenceStore.STRING,
//					PreferenceConstants.EDITOR_FOREGROUND_COLOR),
//			new OverlayPreferenceStore.OverlayKey(
//					OverlayPreferenceStore.BOOLEAN,
//					PreferenceConstants.EDITOR_FOREGROUND_DEFAULT_COLOR),
//			new OverlayPreferenceStore.OverlayKey(
//					OverlayPreferenceStore.STRING,
//					PreferenceConstants.P_BACKGROUND_COLOUR),
//			new OverlayPreferenceStore.OverlayKey(
//					OverlayPreferenceStore.BOOLEAN,
//					PreferenceConstants.EDITOR_BACKGROUND_DEFAULT_COLOR),

			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_STRING_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_STRING_COLOUR_BOLD),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_KEYWORD_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_KEYWORD_COLOUR_BOLD),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_COMMENT_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_COMMENT_COLOUR_BOLD),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_TYPE_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_TYPE_COLOUR_BOLD),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_CONSTANT_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_CONSTANT_COLOUR_BOLD),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_OPERATOR_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_OPERATOR_COLOUR_BOLD),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_DEFAULT_CODE_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_DEFAULT_CODE_COLOUR_BOLD),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_JAVADOC_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_JAVADOC_COLOUR_BOLD),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.STRING,
					PreferenceConstants.P_BUILT_IN_FUNCS_COLOUR),
			new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN,
					PreferenceConstants.P_BUILT_IN_FUNCS_COLOUR_BOLD), };

	private final String[][] fSyntaxColorListModel = new String[][] {
//			{ PreferencesMessages.backgroundColor,
//					PreferenceConstants.P_BACKGROUND_COLOUR },
//			{ PreferencesMessages.nullColor,
//					PreferenceConstants.EDITOR_FOREGROUND_COLOR },
			{ PreferencesMessages.keywordColor,
					PreferenceConstants.P_KEYWORD_COLOUR },
			{ PreferencesMessages.stringColor,
					PreferenceConstants.P_STRING_COLOUR },
			{ PreferencesMessages.typeColor, PreferenceConstants.P_TYPE_COLOUR },
			{ PreferencesMessages.commentsColor,
					PreferenceConstants.P_COMMENT_COLOUR },
			{ PreferencesMessages.constantsColor,
					PreferenceConstants.P_CONSTANT_COLOUR },
			{ PreferencesMessages.defaultCodeColor,
					PreferenceConstants.P_DEFAULT_CODE_COLOUR },
			{ PreferencesMessages.javaDocColor,
					PreferenceConstants.P_JAVADOC_COLOUR },
			{ PreferencesMessages.builtInFuncsColor,
					PreferenceConstants.P_BUILT_IN_FUNCS_COLOUR },
			{ PreferencesMessages.operatorColor,
					PreferenceConstants.P_OPERATOR_COLOUR }, };

	private OverlayPreferenceStore fOverlayStore;

	private Map<ColorEditor, String> fColorButtons = new HashMap<ColorEditor, String>();

	private Map<Button, String> fCheckBoxes = new HashMap<Button, String>();
	private SelectionListener fCheckBoxListener = new SelectionListener()
	{
		public void widgetDefaultSelected(SelectionEvent e)
		{
		}

		public void widgetSelected(SelectionEvent e)
		{
			Button button = (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button
					.getSelection());
		}
	};

	private Map<Text, String> fTextFields = new HashMap<Text, String>();
	private ModifyListener fTextFieldListener = new ModifyListener()
	{
		public void modifyText(ModifyEvent e)
		{
			Text text = (Text) e.widget;
			fOverlayStore.setValue((String) fTextFields.get(text), text
					.getText());
		}
	};

	private ArrayList<Text> fNumberFields = new ArrayList<Text>();
	private ModifyListener fNumberFieldListener = new ModifyListener()
	{
		public void modifyText(ModifyEvent e)
		{
			numberFieldChanged((Text) e.widget);
		}
	};

	private List fSyntaxColorList;
	private ColorEditor fSyntaxForegroundColorEditor;
	private Button fBoldCheckBox;
	private SourceViewer fPreviewViewer;
	private Color fBackgroundColor;

	public PlsqlEditorPreferencePage()
	{
		setDescription(PreferencesMessages.description);
		setPreferenceStore(PlsqleditorPlugin.getDefault().getPreferenceStore());
		fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(), fKeys);
	}

	/*
	 * @see IWorkbenchPreferencePage#init()
	 */
	public void init(IWorkbench workbench)
	{
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		super.createControl(parent);
	}

	private void handleSyntaxColorListSelection()
	{
		int i = fSyntaxColorList.getSelectionIndex();
		String key = fSyntaxColorListModel[i][1];
		RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
		fSyntaxForegroundColorEditor.setColorValue(rgb);
		fBoldCheckBox.setSelection(fOverlayStore.getBoolean(key + BOLD));
	}

	private Control createSyntaxPage(Composite parent)
	{

		Composite colorComposite = new Composite(parent, SWT.NULL);
		colorComposite.setLayout(new GridLayout());

		Label label = new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages.foreground);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite editorComposite = new Composite(colorComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);

		fSyntaxColorList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL
				| SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(5);
		fSyntaxColorList.setLayoutData(gd);

		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		label = new Label(stylesComposite, SWT.LEFT);
		label.setText(PreferencesMessages.color);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		fSyntaxForegroundColorEditor = new ColorEditor(stylesComposite);
		Button foregroundColorButton = fSyntaxForegroundColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText(PreferencesMessages.bold);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fBoldCheckBox.setLayoutData(gd);

		label = new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages.preview);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control previewer = createPreviewer(colorComposite);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = convertWidthInCharsToPixels(20);
		gd.heightHint = convertHeightInCharsToPixels(5);
		previewer.setLayoutData(gd);

		fSyntaxColorList.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// do nothing
			}

			public void widgetSelected(SelectionEvent e)
			{
				handleSyntaxColorListSelection();
			}
		});

		foregroundColorButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// do nothing
			}

			public void widgetSelected(SelectionEvent e)
			{
				int i = fSyntaxColorList.getSelectionIndex();
				String key = fSyntaxColorListModel[i][1];

				PreferenceConverter.setValue(fOverlayStore, key,
						fSyntaxForegroundColorEditor.getColorValue());
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// do nothing
			}

			public void widgetSelected(SelectionEvent e)
			{
				int i = fSyntaxColorList.getSelectionIndex();
				String key = fSyntaxColorListModel[i][1];
				fOverlayStore
						.setValue(key + BOLD, fBoldCheckBox.getSelection());
			}
		});

		return colorComposite;
	}

	private Control createPreviewer(Composite parent)
	{
		final IPreferenceStore chainedStore = new ChainedPreferenceStore(
				new IPreferenceStore[] { EditorsUI.getPreferenceStore(),
						getPreferenceStore(), });

		fPreviewViewer = new SourceViewer(parent, null, SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		fPreviewViewer.configure(new PlSqlConfiguration(chainedStore, null));
		fPreviewViewer.getTextWidget().setFont(
				JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
		fPreviewViewer.setEditable(false);

		initializeViewerColors(fPreviewViewer, chainedStore);

		String content = loadPreviewContentFromFile("ColorSettingPreviewCode.pkb");

		IDocument document = new Document(content);
		IDocumentPartitioner partitioner = PlSqlPartitionScanner
				.createPartitioner(document);
		((IDocumentExtension3) document).setDocumentPartitioner(
				PlsqleditorPlugin.PLSQL_PARTITIONING, partitioner);
		partitioner.connect(document);

		fPreviewViewer.setDocument(document);

		chainedStore.addPropertyChangeListener(new IPropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent event)
			{
				if (fPreviewViewer == null
						|| fPreviewViewer.getTextWidget() == null) return;

				String p = event.getProperty();
				if (p.equals(PreferenceConstants.P_BACKGROUND_COLOUR)
						|| p
								.equals(PreferenceConstants.EDITOR_BACKGROUND_DEFAULT_COLOR))
				{
					initializeViewerColors(fPreviewViewer, chainedStore);
				}

				int topIndex = fPreviewViewer.getTextWidget().getTopIndex();
				int caretOffset = fPreviewViewer.getTextWidget()
						.getCaretOffset();
				fPreviewViewer.unconfigure();
				fPreviewViewer.configure(new PlSqlConfiguration(chainedStore,
						null));

				// Set editor foreground
				fPreviewViewer.getTextWidget().setForeground(
						PlsqleditorPlugin.getDefault().getColor(
								PreferenceConstants.EDITOR_FOREGROUND_COLOR));

				fPreviewViewer.refresh();
				fPreviewViewer.getTextWidget().setTopIndex(topIndex);
				fPreviewViewer.getTextWidget().setCaretOffset(caretOffset);
				fPreviewViewer.invalidateTextPresentation();
				//fPreviewViewer.refresh();
			}
		});

		return fPreviewViewer.getControl();
	}

	/**
	 * Initializes the given viewer's colors.
	 * 
	 * @param viewer
	 *            the viewer to be initialized
	 * @param store
	 *            the preference store from which to read colors
	 */
	private void initializeViewerColors(ISourceViewer viewer,
			IPreferenceStore store)
	{

		StyledText styledText = viewer.getTextWidget();

		// ---------- background color ----------------------
		Color color = store
				.getBoolean(PreferenceConstants.EDITOR_BACKGROUND_DEFAULT_COLOR) ? null
				: createColor(store, PreferenceConstants.P_BACKGROUND_COLOUR,
						styledText.getDisplay());
		styledText.setBackground(color);

		if (fBackgroundColor != null) fBackgroundColor.dispose();

		fBackgroundColor = color;
	}

	/**
	 * Creates a color from the information stored in the given preference
	 * store. Returns <code>null</code> if there is no such information
	 * available.
	 */
	private Color createColor(IPreferenceStore store, String key,
			Display display)
	{

		RGB rgb = null;

		if (store.contains(key))
		{
			if (store.isDefault(key)) rgb = PreferenceConverter
					.getDefaultColor(store, key);
			else rgb = PreferenceConverter.getColor(store, key);

			if (rgb != null) return new Color(display, rgb);
		}

		return null;
	}

	private Control createAppearancePage(Composite parent)
	{

		Composite appearanceComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		appearanceComposite.setLayout(layout);

		addTextField(appearanceComposite,
				PreferencesMessages.displayedTabWidth,
				PreferenceConstants.P_EDITOR_TAB_WIDTH, 4, 0, true);

		// addCheckBox(appearanceComposite,
		// PreferencesMessages.showOverviewRuler,
		// PreferenceConstants.EDITOR_OVERVIEW_RULER, 0);

		addCheckBox(appearanceComposite, PreferencesMessages.wrapLines,
				PreferenceConstants.EDITOR_LINE_WRAP, 0);

		Label l = new Label(appearanceComposite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);

		return appearanceComposite;
	}

	private Control createTypingPage(Composite parent)
	{

		Composite typingComposite = new Composite(parent, SWT.NULL);
		typingComposite.setLayout(new GridLayout());

		Label l = new Label(typingComposite, SWT.LEFT);
		l.setText(PreferencesMessages.autoCompletion);

		addCheckBox(typingComposite, PreferencesMessages.autoCompletionQuote1,
				PreferenceConstants.AUTO_COMPLETION_QUOTE1, 0);

		addCheckBox(typingComposite, PreferencesMessages.autoCompletionQuote2,
				PreferenceConstants.AUTO_COMPLETION_QUOTE2, 0);

		addCheckBox(typingComposite,
				PreferencesMessages.autoCompletionBracket1,
				PreferenceConstants.AUTO_COMPLETION_BRACKET1, 0);

		// addCheckBox(typingComposite,
		// PreferencesMessages.autoCompletionBracket2,
		// PreferenceConstants.AUTO_COMPLETION_BRACKET2, 0);

		addCheckBox(typingComposite,
				PreferencesMessages.autoCompletionBracket3,
				PreferenceConstants.AUTO_COMPLETION_BRACKET3, 0);

		// addCheckBox(typingComposite,
		// PreferencesMessages.autoCompletionBracket4,
		// PreferenceConstants.AUTO_COMPLETION_BRACKET4, 0);

		addFiller(typingComposite);

		addCheckBox(typingComposite, PreferencesMessages.smartHomeEnd,
				PreferenceConstants.EDITOR_SMART_HOME_END, 0);

		// addCheckBox(typingComposite, PreferencesMessages.subWordNavigation,
		// PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION, 0);

		return typingComposite;
	}

	private void addFiller(Composite composite)
	{
		Label filler = new Label(composite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent)
	{
		fOverlayStore.load();
		fOverlayStore.start();

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.general);
		item.setControl(createAppearancePage(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.colors);
		item.setControl(createSyntaxPage(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.typingTabTitle);
		item.setControl(createTypingPage(folder));

		initialize();

		Dialog.applyDialogFont(folder);
		return folder;
	}

	private void initialize()
	{

		initializeFields();

		for (int i = 0; i < fSyntaxColorListModel.length; i++)
		{
			fSyntaxColorList.add(fSyntaxColorListModel[i][0]);
		}
		fSyntaxColorList.getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				if (fSyntaxColorList != null && !fSyntaxColorList.isDisposed())
				{
					fSyntaxColorList.select(0);
					handleSyntaxColorListSelection();
				}
			}
		});
	}

	private void initializeFields()
	{

		Iterator<?> e = fColorButtons.keySet().iterator();
		while (e.hasNext())
		{
			ColorEditor c = (ColorEditor) e.next();
			String key = (String) fColorButtons.get(c);
			RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
			c.setColorValue(rgb);
		}

		e = fCheckBoxes.keySet().iterator();
		while (e.hasNext())
		{
			Button b = (Button) e.next();
			String key = (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}

		e = fTextFields.keySet().iterator();
		while (e.hasNext())
		{
			Text t = (Text) e.next();
			String key = (String) fTextFields.get(t);
			t.setText(fOverlayStore.getString(key));
		}
	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk()
	{
		fOverlayStore.propagate();
		PlsqleditorPlugin.getDefault().savePluginPreferences();
		//InstanceScope.getNode(&lt;bundleId&gt;).flush()
		return true;
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults()
	{

		fOverlayStore.loadDefaults();

		initializeFields();

		handleSyntaxColorListSelection();

		super.performDefaults();

		fPreviewViewer.invalidateTextPresentation();
	}

	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose()
	{

		if (fOverlayStore != null)
		{
			fOverlayStore.stop();
			fOverlayStore = null;
		}
		if (fBackgroundColor != null && !fBackgroundColor.isDisposed())
		{
			fBackgroundColor.dispose();
		}

		super.dispose();
	}

	private Button addCheckBox(Composite parent, String label, String key,
			int indentation)
	{
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);

		fCheckBoxes.put(checkBox, key);

		return checkBox;
	}

	private Text addTextField(Composite composite, String label, String key,
			int textLimit, int indentation, boolean isNumber)
	{
		return getTextControl(addLabelledTextField(composite, label, key,
				textLimit, indentation, isNumber));
	}

	private static Text getTextControl(Control[] labelledTextField)
	{
		return (Text) labelledTextField[1];
	}

	/**
	 * Returns an array of size 2: - first element is of type <code>Label</code>
	 * - second element is of type <code>Text</code> Use
	 * <code>getLabelControl</code> and <code>getTextControl</code> to get the 2
	 * controls.
	 */
	private Control[] addLabelledTextField(Composite composite, String label,
			String key, int textLimit, int indentation, boolean isNumber)
	{
		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indentation;
		labelControl.setLayoutData(gd);

		Text textControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		fTextFields.put(textControl, key);
		if (isNumber)
		{
			fNumberFields.add(textControl);
			textControl.addModifyListener(fNumberFieldListener);
		}
		else
		{
			textControl.addModifyListener(fTextFieldListener);
		}

		return new Control[] { labelControl, textControl };
	}

	private String loadPreviewContentFromFile(String filename)
	{
		String line;
		String separator = System.getProperty("line.separator");
		StringBuffer buffer = new StringBuffer(512);
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(getClass()
					.getResourceAsStream(filename)));
			while ((line = reader.readLine()) != null)
			{
				buffer.append(line);
				buffer.append(separator);
			}
		}
		catch (IOException io)
		{
			// 
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		return buffer.toString();
	}

	private void numberFieldChanged(Text textControl)
	{
		String number = textControl.getText();
		IStatus status = validatePositiveNumber(number);
		if (!status.matches(IStatus.ERROR))
			fOverlayStore.setValue((String) fTextFields.get(textControl),
					number);
		updateStatus(status);
	}

	private IStatus validatePositiveNumber(String number)
	{
		StatusInfo status = new StatusInfo();
		if (number.length() == 0)
		{
			status.setError(PreferencesMessages.emptyInput);
		}
		else
		{
			try
			{
				int value = Integer.parseInt(number);
				if (value < 0)
					status.setError(PreferencesMessages.bind(
							PreferencesMessages.invalidInput, number));
			}
			catch (NumberFormatException e)
			{
				status.setError(PreferencesMessages.bind(
						PreferencesMessages.invalidInput, number));
			}
		}
		return status;
	}

	void updateStatus(IStatus status)
	{
		if (!status.matches(IStatus.ERROR))
		{
			for (int i = 0; i < fNumberFields.size(); i++)
			{
				Text text = (Text) fNumberFields.get(i);
				IStatus s = validatePositiveNumber(text.getText());
				status = StatusUtil.getMoreSevere(s, status);
			}
		}

		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}
}