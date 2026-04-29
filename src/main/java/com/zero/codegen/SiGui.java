package com.zero.codegen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

public class SiGui extends JFrame {
    private static final String PREF_JAVA_OUT="javaOut";
    private static final String PREF_CS_OUT="csOut";
    private static final String PREF_PKG="pkg";
    private static final String PREF_PROTO_ID="protoId";
    private static final String PREF_BO_OUT="boOut";
    private static final String PREF_BO_PKG="boPkg";
    private static final String PREF_CS_NS="csNs";
    private static final String PREF_GEN_JAVA="genJava";
    private static final String PREF_GEN_CS="genCs";
    private static final String PREF_GEN_BO_IMPL="genBoImpl";
    private static final String PREF_IMPL_COMPONENT="implComponent";
    private static final String PREF_GEN_AUTO_CONFIG="genAutoConfig";
    private static final String PREF_SCAN_IMPL="scanImpl";
    private static final String PREF_SIMD="simd";  // SIMD向量化开关
    private static final String PREF_FILES="files";

    private final Preferences prefs=Preferences.userRoot().node("com.zero.codegen.SiGui");
    private final DefaultListModel<String> siModel=new DefaultListModel<>();
    private final Set<String> trackedFiles=new LinkedHashSet<>();

    private final JList<String> siList=new JList<>(siModel);
    private final JTextArea logArea=new JTextArea();
    private final JLabel statusLabel=new JLabel("Ready");

    private final JTextField javaOut=new JTextField();
    private final JTextField csOut=new JTextField();
    private final JTextField pkg=new JTextField("com.zero.protocol");
    private final JTextField protoId=new JTextField();
    private final JTextField boOut=new JTextField();
    private final JTextField boPkg=new JTextField("com.zero.protocol.bo");
    private final JTextField csNs=new JTextField();

    private final JCheckBox cbJava=new JCheckBox("Generate Java", true);
    private final JCheckBox cbCs=new JCheckBox("Generate C#", false);
    private final JCheckBox cbBoImpl=new JCheckBox("Generate BO impl", false);
    private final JCheckBox cbImplComponent=new JCheckBox("Annotate impl with @Component", false);
    private final JCheckBox cbGenAutoConfig=new JCheckBox("Generate auto config", true);
    private final JCheckBox cbScanImpl=new JCheckBox("Scan BO impl package", true);
    private final JCheckBox cbSimd=new JCheckBox("Enable SIMD Vectorization", false);

    private final JButton addFilesButton=new JButton("Add Files");
    private final JButton addFolderButton=new JButton("Add Folder");
    private final JButton removeButton=new JButton("Remove");
    private final JButton clearButton=new JButton("Clear");
    private final JButton checkButton=new JButton("Validate");
    private final JButton generateButton=new JButton("Generate");

    private SwingWorker<?, ?> activeWorker;

    public SiGui(){
        super("Zero SI Codegen");
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch (Exception ignored){
        }
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 760));
        setSize(1180, 820);
        setLocationRelativeTo(null);
        setContentPane(buildRoot());
        bindActions();
        loadPrefs();
        applyToggle();
        appendLog("Workspace: "+getProjectDir());
    }

    private JComponent buildRoot(){
        JPanel root=new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildCenter(){
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildFilesPanel(), buildSettingsPanel());
        split.setResizeWeight(0.34d);
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createEmptyBorder());
        return split;
    }

    private JComponent buildFilesPanel(){
        JPanel panel=new JPanel(new BorderLayout(8, 8));
        panel.setBorder(sectionBorder("SI Files"));

        siList.setVisibleRowCount(16);
        siList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        siList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                JLabel label=(JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                File file=new File(String.valueOf(value));
                label.setText(file.getName());
                label.setToolTipText(file.getAbsolutePath());
                return label;
            }
        });

        JScrollPane scrollPane=new JScrollPane(siList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.add(addFilesButton);
        actions.add(addFolderButton);
        actions.add(removeButton);
        actions.add(clearButton);
        panel.add(actions, BorderLayout.NORTH);
        return panel;
    }

    private JComponent buildSettingsPanel(){
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildConfigScroll(), buildLogPanel());
        split.setResizeWeight(0.62d);
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createEmptyBorder());
        return split;
    }

    private JComponent buildConfigScroll(){
        JPanel content=new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildTargetsPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildPathsPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildJavaPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildCSharpPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildOptionPanel());
        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane=new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JComponent buildTargetsPanel(){
        JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panel.setBorder(sectionBorder("Targets"));
        panel.add(cbJava);
        panel.add(cbCs);
        panel.add(cbBoImpl);
        return panel;
    }

    private JComponent buildPathsPanel(){
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(sectionBorder("Shared Inputs"));
        GridBagConstraints gbc=createFormConstraints();
        addRow(panel, gbc, 0, "protoId.txt", protoId, e -> chooseFile(protoId, "txt"));
        return panel;
    }

    private JComponent buildJavaPanel(){
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(sectionBorder("Java"));
        GridBagConstraints gbc=createFormConstraints();
        addRow(panel, gbc, 0, "Protocol output", javaOut, e -> chooseDirectory(javaOut));
        addRow(panel, gbc, 1, "Protocol package", pkg, null);
        addRow(panel, gbc, 2, "BO output", boOut, e -> chooseDirectory(boOut));
        addRow(panel, gbc, 3, "BO package", boPkg, null);
        return panel;
    }

    private JComponent buildCSharpPanel(){
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(sectionBorder("C#"));
        GridBagConstraints gbc=createFormConstraints();
        addRow(panel, gbc, 0, "C# output", csOut, e -> chooseDirectory(csOut));
        addRow(panel, gbc, 1, "Namespace", csNs, null);
        return panel;
    }

    private JComponent buildOptionPanel(){
        JPanel panel=new JPanel(new GridLayout(0, 1, 0, 6));
        panel.setBorder(sectionBorder("Java BO Options"));
        panel.add(cbImplComponent);
        panel.add(cbGenAutoConfig);
        panel.add(cbScanImpl);
        panel.add(cbSimd);
        return panel;
    }

    private JComponent buildLogPanel(){
        JPanel panel=new JPanel(new BorderLayout(8, 8));
        panel.setBorder(sectionBorder("Task Log"));
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildFooter(){
        JPanel footer=new JPanel(new BorderLayout(8, 8));
        footer.setBorder(new EmptyBorder(4, 0, 0, 0));
        footer.add(statusLabel, BorderLayout.WEST);

        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(checkButton);
        actions.add(generateButton);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private void bindActions(){
        cbJava.addActionListener(e -> applyToggle());
        cbCs.addActionListener(e -> applyToggle());
        cbBoImpl.addActionListener(e -> applyToggle());
        cbGenAutoConfig.addActionListener(e -> applyToggle());

        addFilesButton.addActionListener(this::onAddFiles);
        addFolderButton.addActionListener(this::onAddFolder);
        removeButton.addActionListener(e -> removeSelectedFiles());
        clearButton.addActionListener(e -> clearFiles());
        checkButton.addActionListener(e -> onCheck());
        generateButton.addActionListener(e -> onGenerate());
    }

    private void onAddFiles(ActionEvent event){
        JFileChooser chooser=new JFileChooser(new File(getProjectDir()));
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        for(File file: chooser.getSelectedFiles()){
            if(file.getName().endsWith(".si")){
                addTrackedFile(file.toPath());
            }
        }
    }

    private void onAddFolder(ActionEvent event){
        JFileChooser chooser=new JFileChooser(new File(getProjectDir()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        File selected=chooser.getSelectedFile();
        try(Stream<Path> stream=Files.walk(selected.toPath())){
            stream.filter(path -> path.toString().endsWith(".si")).forEach(this::addTrackedFile);
            appendLog("Added folder: "+selected.getAbsolutePath());
        }catch (Exception ex){
            showError("Failed to scan folder", ex);
        }
    }

    private void removeSelectedFiles(){
        List<String> selected=siList.getSelectedValuesList();
        if(selected.isEmpty()) return;
        trackedFiles.removeAll(selected);
        syncFileModel();
    }

    private void clearFiles(){
        trackedFiles.clear();
        syncFileModel();
    }

    private void onCheck(){
        runTask("Validate", () -> {
            List<String> files=listFiles();
            if(files.isEmpty()){
                throw new IllegalArgumentException("Add at least one .si file first.");
            }
            for(String file: files){
                String text=Files.readString(Path.of(file));
                String normalized=SiCompiler.preprocess(text);
                boolean hasDefinitions=!SiCompiler.parseEnums(normalized).isEmpty()
                        || !SiCompiler.parseStructs(normalized).isEmpty()
                        || SiCompiler.parseProto(normalized)!=null;
                if(!hasDefinitions){
                    throw new IllegalArgumentException("No enum/struct/proto definition found: "+file);
                }
            }
            return "Validation passed for "+files.size()+" file(s).";
        });
    }

    private void onGenerate(){
        runTask("Generate", () -> {
            List<String> files=listFiles();
            if(files.isEmpty()){
                throw new IllegalArgumentException("Add at least one .si file first.");
            }
            if(!cbJava.isSelected() && !cbCs.isSelected()){
                throw new IllegalArgumentException("Select at least one target.");
            }

            String javaOutValue=javaOut.getText().isBlank()? getProjectDir() : javaOut.getText().trim();
            String csOutValue=csOut.getText().isBlank()? getProjectDir() : csOut.getText().trim();
            String boOutValue=boOut.getText().isBlank()? javaOutValue : boOut.getText().trim();
            String pkgValue=pkg.getText().trim();
            String boPkgValue=boPkg.getText().trim();
            String protoIdValue=protoId.getText().trim();
            String csNsValue=csNs.getText().trim();

            if(cbJava.isSelected()){
                if(pkgValue.isBlank()){
                    throw new IllegalArgumentException("Java protocol package is required.");
                }
                if(boPkgValue.isBlank()){
                    throw new IllegalArgumentException("Java BO package is required.");
                }
            }

            List<String> warnings=SiCompiler.compileBatch(
                    files,
                    cbJava.isSelected()? javaOutValue : "",
                    pkgValue,
                    protoIdValue,
                    cbJava.isSelected(),
                    cbCs.isSelected(),
                    cbCs.isSelected()? csOutValue : "",
                    cbJava.isSelected()? boOutValue : "",
                    cbJava.isSelected()? boPkgValue : "",
                    cbCs.isSelected()? csNsValue : "",
                    cbJava.isSelected() && cbBoImpl.isSelected(),
                    cbJava.isSelected() && cbImplComponent.isSelected(),
                    cbJava.isSelected() && cbGenAutoConfig.isSelected(),
                    cbJava.isSelected() && cbScanImpl.isSelected(),
                    cbSimd.isSelected()
            );
            savePrefs();
            if(warnings.isEmpty()){
                return "Code generation finished without warnings.";
            }
            return "Code generation finished with warnings:\n- "+String.join("\n- ", warnings);
        });
    }

    private void runTask(String name, CheckedTask task){
        if(activeWorker!=null){
            appendLog("A task is already running.");
            return;
        }
        SwingWorker<String, Void> worker=new SwingWorker<String, Void>(){
            @Override
            protected String doInBackground() throws Exception{
                return task.run();
            }

            @Override
            protected void done(){
                try{
                    String message=get();
                    appendLog(message);
                    statusLabel.setText(name+" finished");
                    if(message.contains("warnings:\n- ")){
                        JOptionPane.showMessageDialog(SiGui.this, message, name, JOptionPane.WARNING_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(SiGui.this, message, name, JOptionPane.INFORMATION_MESSAGE);
                    }
                }catch (Exception ex){
                    Throwable cause=ex.getCause()==null? ex : ex.getCause();
                    showError(name+" failed", cause);
                }finally{
                    activeWorker=null;
                    setBusy(false, "Ready");
                }
            }
        };
        activeWorker=worker;
        setBusy(true, name+"...");
        appendLog(name+" started.");
        worker.execute();
    }

    private void setBusy(boolean busy, String status){
        statusLabel.setText(status);
        addFilesButton.setEnabled(!busy);
        addFolderButton.setEnabled(!busy);
        removeButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);
        checkButton.setEnabled(!busy);
        generateButton.setEnabled(!busy);
        siList.setEnabled(!busy);
        cbJava.setEnabled(!busy);
        cbCs.setEnabled(!busy);
        cbBoImpl.setEnabled(!busy && cbJava.isSelected());
        cbImplComponent.setEnabled(!busy && cbJava.isSelected() && cbBoImpl.isSelected());
        cbGenAutoConfig.setEnabled(!busy && cbJava.isSelected());
        cbScanImpl.setEnabled(!busy && cbJava.isSelected() && cbGenAutoConfig.isSelected());
        applyToggle();
    }

    private void addTrackedFile(Path path){
        trackedFiles.add(path.toAbsolutePath().normalize().toString());
        syncFileModel();
    }

    private void syncFileModel(){
        siModel.clear();
        for(String path: trackedFiles){
            siModel.addElement(path);
        }
        savePrefs();
    }

    private List<String> listFiles(){
        return new ArrayList<>(trackedFiles);
    }

    private void applyToggle(){
        boolean javaEnabled=cbJava.isSelected() && activeWorker==null;
        boolean csEnabled=cbCs.isSelected() && activeWorker==null;

        javaOut.setEnabled(javaEnabled);
        pkg.setEnabled(javaEnabled);
        boOut.setEnabled(javaEnabled);
        boPkg.setEnabled(javaEnabled);
        cbBoImpl.setEnabled(javaEnabled);

        boolean boImplEnabled=javaEnabled && cbBoImpl.isSelected();
        cbImplComponent.setEnabled(boImplEnabled);
        cbGenAutoConfig.setEnabled(javaEnabled);
        cbScanImpl.setEnabled(javaEnabled && cbGenAutoConfig.isSelected());

        csOut.setEnabled(csEnabled);
        csNs.setEnabled(csEnabled);
        protoId.setEnabled(activeWorker==null);
    }

    private void savePrefs(){
        prefs.put(PREF_JAVA_OUT, javaOut.getText());
        prefs.put(PREF_CS_OUT, csOut.getText());
        prefs.put(PREF_PKG, pkg.getText());
        prefs.put(PREF_PROTO_ID, protoId.getText());
        prefs.put(PREF_BO_OUT, boOut.getText());
        prefs.put(PREF_BO_PKG, boPkg.getText());
        prefs.put(PREF_CS_NS, csNs.getText());
        prefs.putBoolean(PREF_GEN_JAVA, cbJava.isSelected());
        prefs.putBoolean(PREF_GEN_CS, cbCs.isSelected());
        prefs.putBoolean(PREF_GEN_BO_IMPL, cbBoImpl.isSelected());
        prefs.putBoolean(PREF_IMPL_COMPONENT, cbImplComponent.isSelected());
        prefs.putBoolean(PREF_GEN_AUTO_CONFIG, cbGenAutoConfig.isSelected());
        prefs.putBoolean(PREF_SCAN_IMPL, cbScanImpl.isSelected());
        prefs.putBoolean(PREF_SIMD, cbSimd.isSelected());
        prefs.put(PREF_FILES, String.join("\n", trackedFiles));
    }

    private void loadPrefs(){
        javaOut.setText(prefs.get(PREF_JAVA_OUT, ""));
        csOut.setText(prefs.get(PREF_CS_OUT, ""));
        pkg.setText(prefs.get(PREF_PKG, "com.zero.protocol"));
        protoId.setText(prefs.get(PREF_PROTO_ID, ""));
        boOut.setText(prefs.get(PREF_BO_OUT, ""));
        boPkg.setText(prefs.get(PREF_BO_PKG, "com.zero.protocol.bo"));
        csNs.setText(prefs.get(PREF_CS_NS, ""));
        cbJava.setSelected(prefs.getBoolean(PREF_GEN_JAVA, true));
        cbCs.setSelected(prefs.getBoolean(PREF_GEN_CS, false));
        cbBoImpl.setSelected(prefs.getBoolean(PREF_GEN_BO_IMPL, false));
        cbImplComponent.setSelected(prefs.getBoolean(PREF_IMPL_COMPONENT, false));
        cbGenAutoConfig.setSelected(prefs.getBoolean(PREF_GEN_AUTO_CONFIG, true));
        cbScanImpl.setSelected(prefs.getBoolean(PREF_SCAN_IMPL, true));
        cbSimd.setSelected(prefs.getBoolean(PREF_SIMD, false));

        trackedFiles.clear();
        String savedFiles=prefs.get(PREF_FILES, "");
        if(!savedFiles.isBlank()){
            for(String line: savedFiles.split("\\R")){
                String trimmed=line.trim();
                if(!trimmed.isEmpty()){
                    trackedFiles.add(trimmed);
                }
            }
        }
        syncFileModel();
    }

    private void chooseDirectory(JTextField target){
        JFileChooser chooser=new JFileChooser(new File(getProjectDir()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
            target.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseFile(JTextField target, String expectedExtension){
        JFileChooser chooser=new JFileChooser(new File(getProjectDir()));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        File selected=chooser.getSelectedFile();
        if(expectedExtension==null || selected.getName().toLowerCase().endsWith("."+expectedExtension.toLowerCase())){
            target.setText(selected.getAbsolutePath());
        }
    }

    private void appendLog(String message){
        String prefix="["+LocalTime.now().withNano(0)+"] ";
        logArea.append(prefix+message+"\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(String title, Throwable throwable){
        appendLog(title+": "+throwable);
        StringWriter sw=new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        JTextArea area=new JTextArea(sw.toString(), 24, 90);
        area.setEditable(false);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title, JOptionPane.ERROR_MESSAGE);
    }

    private String getProjectDir(){
        return Paths.get("").toAbsolutePath().normalize().toString();
    }

    private GridBagConstraints createFormConstraints(){
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(4, 4, 4, 4);
        gbc.anchor=GridBagConstraints.WEST;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field, java.awt.event.ActionListener chooser){
        gbc.gridx=0;
        gbc.gridy=row;
        gbc.weightx=0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx=1;
        gbc.weightx=1;
        panel.add(field, gbc);

        if(chooser!=null){
            JButton browse=new JButton("Browse");
            browse.addActionListener(chooser);
            gbc.gridx=2;
            gbc.weightx=0;
            panel.add(browse, gbc);
        }
    }

    private javax.swing.border.Border sectionBorder(String title){
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                new EmptyBorder(6, 6, 6, 6)
        );
    }

    public static void main(String[] args){
        EventQueue.invokeLater(() -> new SiGui().setVisible(true));
    }

    @FunctionalInterface
    private interface CheckedTask{
        String run() throws Exception;
    }
}
