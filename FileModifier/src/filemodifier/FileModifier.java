/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filemodifier;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 *
 * @author filip
 */
public class FileModifier extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
    public FileModifier() {
        initComponents();
    }

    private int countChainsInFile(String path) {
        int found = 0;
        byte[] bytesArray = getBytesFromString(firstChain.getText());
        try {
            found = findChainInFile(bytesArray, path).size();
        } catch (IOException ex) {
            Logger.getLogger(FileModifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return found;
    }

    private byte[] getBytesBefore(byte[] file, int start) {
        byte[] result = new byte[start];
        for (int i = 0; i < start; i++) {
            result[i] = file[i];
        }
        return result;
    }

    private byte[] getBytesAfter(byte[] file, int end) {
        byte[] result = new byte[file.length - end - 1];
        for (int i = end + 1, k = 0; i < file.length; i++) {
            result[k++] = file[i];
        }
        return result;
    }

    private byte[] mergeArrays(byte[] arr1, byte[] arr2, byte[] arr3) {
        int totalLength = arr1.length + arr2.length + arr3.length;
        int pos = 0;
        byte[] result = new byte[totalLength];

        for (Byte element : arr1) //copying elements of secondArray using for-each loop  
        {
            result[pos] = element;
            pos++;              //increases position by 1  
        }
        for (Byte element : arr2) //copying elements of firstArray using for-each loop  
        {
            result[pos] = element;
            pos++;
        }
        for (Byte element : arr3) //copying elements of firstArray using for-each loop  
        {
            result[pos] = element;
            pos++;
        }
        return result;
    }

    private boolean isChainInFile(byte[] chain, String path) throws IOException {
        if(findChainInFile(chain, path).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void replaceChain(String path, byte[] chain, byte[] chainToReplace) throws IOException {
        ArrayList<Position> positions = findChainInFile(chain, path);
        int moveIndex = 0;
        byte[] file = Files.readAllBytes(Paths.get(path));
        for (int i = 0; i < positions.size(); i++) {
            if (i == 0) {
                byte[] start = getBytesBefore(file, positions.get(i).getStart() + moveIndex);
                byte[] end = getBytesAfter(file, positions.get(i).getEnd() + moveIndex);
                byte[] mergedArray = mergeArrays(start, chainToReplace, end);
                file = mergedArray;
            } else {
                moveIndex += chainToReplace.length - chain.length;
                byte[] start = getBytesBefore(file, positions.get(i).getStart() + moveIndex);
                byte[] end = getBytesAfter(file, positions.get(i).getEnd() + moveIndex);
                byte[] mergedArray = mergeArrays(start, chainToReplace, end);
                file = mergedArray;
            }
        }
        Files.write(Paths.get(path), file);
        showFilesInTable();
    }

    private void chooseFile() {
        javax.swing.UIManager.put("FileChooser.openButtonText", "Wybierz");
        javax.swing.UIManager.put("FileChooser.cancelButtonText", "Anuluj");
        javax.swing.UIManager.put("FileChooser.lookInLabelText", "Wyszukaj w");
        javax.swing.UIManager.put("FileChooser.folderNameLabelText", "Nazwa folderu");
        javax.swing.UIManager.put("FileChooser.filesOfTypeLabelText", "Typ pliku");

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(""));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            pathFile.setText(chooser.getSelectedFile().toString());
            pathFile.setBackground(Color.WHITE);
        } else {
            System.out.println("No Selection ");
        }
    }

    private ArrayList<Position> findChainInFile(byte[] chain, String path) throws IOException {
        ArrayList<Position> foundChains = new ArrayList<>();
        int j = 0;
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        int lengthOfBytesInFile = bytes.length;
        int chainLength = chain.length;
        int positiveCount = 0;

        if (lengthOfBytesInFile >= chainLength) {
            for (int i = 0; i <= lengthOfBytesInFile - 1; i++) {
                if (lengthOfBytesInFile >= i + chainLength - 1) {
                    if (bytes[i] == chain[j]) { 
                        for (int k = i; k <= i + chainLength - 1; k++) {
                            if (bytes[k] == chain[j]) {
                                positiveCount++;
                                j++;
                            } else {
                                positiveCount = 0;
                                j = 0;
                            }
                        }
                        if (positiveCount == chainLength) {
                            foundChains.add(new Position(i, i + chainLength - 1));
                            positiveCount = 0;
                            j = 0;
                        } else {
                            System.out.println("Ciag odrzucono");
                        }
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Podany ciąg wyszukiwanych bajtów jest dłuższy od ciągu w zawartym pliku. " + path);
        }

        if (foundChains.isEmpty()) {
            System.out.println("Brak podanych ciągów w pliku");
        } else {
            System.out.println("Znalezione ciągi w pliku: " + path + " \n" + +foundChains.size());
        }
        return foundChains;
    }

    private void clearTable(DefaultTableModel model) {

        if (tabela.getRowCount() > 0) {
            for (int i = tabela.getRowCount() - 1; i > -1; i--) {
                model.removeRow(i);
            }
        }
    }

    private void showFilesInTable() {
        String[] rozszerzenia = rozszerzenie.getText().replaceAll(" ", "").split(",");
        DefaultTableModel model = (DefaultTableModel) tabela.getModel();
        clearTable(model);
        try {
            for (String r : rozszerzenia) {
                Files.walk(Paths.get(pathFile.getText()))
                        .filter(Files::isRegularFile)
                        .forEach((f) -> {
                            String file = f.toString();
                            if (file.endsWith(r)) {
                                model.addRow(new Object[]{model.getRowCount() + 1, file});
                            }
                        });
            }

        } catch (IOException ex) {
            Logger.getLogger(FileModifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        tabela.getColumnModel().getColumn(0).setPreferredWidth(15);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(280);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(15);

    }

    private void showFileBytes(String path) {
        LinkedList<Byte> bytes = new LinkedList<>();
        try {
            byte[] byteArrray = Files.readAllBytes(Paths.get(path));
            for (Byte b : byteArrray) {
                bytes.add(b);

            }

        } catch (IOException ex) {
            Logger.getLogger(FileModifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        ByteViewer.setLocationRelativeTo(null);
        ByteViewer.setVisible(true);
        ByteViewer.setSize(650, 400);
        bytesTextArea.setText(bytes.toString());
        viewedFileTextfield.setText(path);
    }

    private boolean isAByte(String data, JTextField textField) {
        String[] dataArray = data.replaceAll(" ", "").split(",");
        boolean result = false;
        for (String element : dataArray) {
            try {
                Byte.parseByte(element);
                result = true;
            } catch (Exception e) {
                result = false;
                textField.setBackground(Color.red);
                JOptionPane.showMessageDialog(rootPane, "Element: " + element + " nie jest wartością wyrażoną w bajtach.");
                break;
            }
        }
        return result;
    }

    private boolean isDataEmpty(String data, JTextField textField) {
        if (data.equals("")) {
            textField.setBackground(Color.red);
            JOptionPane.showMessageDialog(rootPane, "To pole nie może być puste!");
            return true;
        } else {
            return false;
        }
    }

    private byte[] getBytesFromString(String data) {
        String[] stringArray = data.replaceAll(" ", "").split(",");
        LinkedList<Byte> bytes = new LinkedList<>();
        for (String s : stringArray) {
            bytes.add(Byte.parseByte(s));
        }
        byte[] chain = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++) {
            chain[i] = bytes.get(i);
        }
        return chain;
    }

    private boolean isTableEmpty() {
        if (tabela.getRowCount() <= 0) {
            JOptionPane.showMessageDialog(rootPane, "Wskaż folder oraz wyświetl pliki");
            return true;
        } else {
            return false;
        }
    }

    private boolean confirmationAlert() {
        Object options[] = {"Tak", "Nie"};
        int p = JOptionPane.showOptionDialog(null, "Uwaga! \nZmiana ciągu bajtów może wywołać nieodwracalne zmiany w postaci uszkodzenia plików. \n\nCzy jesteś pewien, że chcesz wykonać danć operację?", "Ostrzeżenie", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        if (p == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void closeApplication() {
        WindowEvent winClosingEvent = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(winClosingEvent);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ByteViewer = new javax.swing.JDialog();
        closeByteViewer = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        bytesTextArea = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        viewedFileTextfield = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        showFilesButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        rozszerzenie = new javax.swing.JTextField();
        chooseFileButton = new javax.swing.JButton();
        pathFile = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        firstChain = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        secondChain = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        findBytesChainButton = new javax.swing.JButton();
        replaceBytesChainButton = new javax.swing.JButton();
        showBytesChainButton = new javax.swing.JButton();
        replaceChainInOneFileButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        closeByteViewer.setText("zamknij");
        closeByteViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeByteViewerActionPerformed(evt);
            }
        });

        bytesTextArea.setColumns(20);
        bytesTextArea.setRows(5);
        jScrollPane2.setViewportView(bytesTextArea);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setText("Wyświetlany plik:");

        viewedFileTextfield.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(viewedFileTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewedFileTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap())
        );

        javax.swing.GroupLayout ByteViewerLayout = new javax.swing.GroupLayout(ByteViewer.getContentPane());
        ByteViewer.getContentPane().setLayout(ByteViewerLayout);
        ByteViewerLayout.setHorizontalGroup(
            ByteViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ByteViewerLayout.createSequentialGroup()
                .addContainerGap(561, Short.MAX_VALUE)
                .addComponent(closeByteViewer)
                .addContainerGap())
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ByteViewerLayout.setVerticalGroup(
            ByteViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ByteViewerLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closeByteViewer, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        setForeground(java.awt.Color.lightGray);

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "L.p", "Plik", "Ilość ciągów"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabela);

        showFilesButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        showFilesButton.setText("Wyświetl pliki");
        showFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showFilesButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel1.setText("Ścieżka");

        rozszerzenie.setToolTipText("Podaj rozszerzenia wyszukiwanych plików po przecinku (np. txt,pdf,xml)");
        rozszerzenie.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                rozszerzenieFocusGained(evt);
            }
        });

        chooseFileButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        chooseFileButton.setText("Wybierz folder");
        chooseFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileButtonActionPerformed(evt);
            }
        });

        pathFile.setToolTipText("Wskaż ścieżkę przeszukiwanego katalogu.");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("Rozszerzenia szukanych plików");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(rozszerzenie, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(43, 43, 43)
                                .addComponent(pathFile)))
                        .addGap(18, 18, 18)
                        .addComponent(chooseFileButton))
                    .addComponent(showFilesButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(chooseFileButton))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rozszerzenie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(showFilesButton)
                .addContainerGap())
        );

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel4.setText("Docelowy ciąg bajtów");

        firstChain.setToolTipText("Wprowadź wartości po przecinku z zakresu liczb -128 do 127 (np. 60,51,44,88,-50...) ");
        firstChain.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                firstChainFocusGained(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setText("Szukany ciąg bajtów");

        secondChain.setToolTipText("Wprowadź wartości po przecinku z zakresu liczb -128 do 127 (np. 60,51,44,88,-50...) ");
        secondChain.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                secondChainFocusGained(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(50, 50, 50)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(secondChain)
                            .addComponent(firstChain))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(firstChain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(secondChain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        findBytesChainButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        findBytesChainButton.setText("Wyszukaj ciągi bajtów");
        findBytesChainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findBytesChainButtonActionPerformed(evt);
            }
        });

        replaceBytesChainButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        replaceBytesChainButton.setText("Zamień ciągi we wszystkich plikach");
        replaceBytesChainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceBytesChainButtonActionPerformed(evt);
            }
        });

        showBytesChainButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        showBytesChainButton.setText("Pokaż ciąg bajtów");
        showBytesChainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBytesChainButtonActionPerformed(evt);
            }
        });

        replaceChainInOneFileButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        replaceChainInOneFileButton.setText("Zamień ciąg w wybranym pliku");
        replaceChainInOneFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceChainInOneFileButtonActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButton1.setText("Wyjdź z programu");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(replaceChainInOneFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(replaceBytesChainButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(findBytesChainButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showBytesChainButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showBytesChainButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(findBytesChainButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(replaceChainInOneFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(replaceBytesChainButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        setSize(new java.awt.Dimension(916, 539));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void chooseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFileButtonActionPerformed
        chooseFile();
    }//GEN-LAST:event_chooseFileButtonActionPerformed

    private void showFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showFilesButtonActionPerformed
        if (!isDataEmpty(pathFile.getText(), pathFile)) {
            if (rozszerzenie.getText().equals("")) {
                Object options[] = {"Tak", "Nie"};
                int p = JOptionPane.showOptionDialog(null, "Nie wskazano rozszerzenia. Czy wyszukać wszystkie pliki w katalogu?", "Rozszerzenie", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (p == 0) {
                    showFilesInTable();
                    rozszerzenie.setBackground(Color.WHITE);
                    pathFile.setBackground(Color.WHITE);
                } else {
                    rozszerzenie.setBackground(Color.orange);
                }
            } else {
                showFilesInTable();
            }
        }
    }//GEN-LAST:event_showFilesButtonActionPerformed

    private void replaceBytesChainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceBytesChainButtonActionPerformed
        int replacedChains = 0;
        if (!isDataEmpty(firstChain.getText(), firstChain) && !isDataEmpty(secondChain.getText(), secondChain) && !isTableEmpty()) {
            if (isAByte(firstChain.getText(), firstChain) && isAByte(secondChain.getText(), secondChain)) {
                byte[] searchedChain = getBytesFromString(firstChain.getText());
                byte[] replaceChain = getBytesFromString(secondChain.getText());
                if (confirmationAlert()) {
                    try {
                        for (int row = 0; row < tabela.getRowCount(); row++) {
                            String path = tabela.getValueAt(row, 1).toString();
                            if (isChainInFile(searchedChain, path)) {
                                replaceChain(path, searchedChain, replaceChain);
                                replacedChains++;
                            }
                        }
                        if (replacedChains > 0) {
                            JOptionPane.showMessageDialog(rootPane, "Zamieniono ciągi bajtów w: " + replacedChains + " plikach");
                        } else {
                            JOptionPane.showMessageDialog(rootPane, "Nie znaleziono pliku z wskazanym ciągiem bajtów");
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(FileModifier.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }//GEN-LAST:event_replaceBytesChainButtonActionPerformed

    private void showBytesChainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showBytesChainButtonActionPerformed
        try {
            int row = tabela.getSelectedRow();
            if (row >= 0) {
                String path = (tabela.getModel().getValueAt(row, 1).toString());
                showFileBytes(path);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }//GEN-LAST:event_showBytesChainButtonActionPerformed

    private void findBytesChainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findBytesChainButtonActionPerformed
        if (!isDataEmpty(firstChain.getText(), firstChain) && !isTableEmpty()) {
            if (isAByte(firstChain.getText(), firstChain)) {
                int fileCount = 0;
                long foundChainsCount = 0;
                for (int row = 0; row < tabela.getRowCount(); row++) {
                    int val = countChainsInFile(tabela.getValueAt(row, 1).toString());
                    tabela.setValueAt(val, row, 2);
                }
                for (int row = 0; row < tabela.getRowCount(); row++) {
                    if (Integer.parseInt(tabela.getValueAt(row, 2).toString()) > 0) {
                        fileCount++;
                        foundChainsCount += Long.parseLong(tabela.getValueAt(row, 2).toString());
                    }

                }
                JOptionPane.showMessageDialog(null, "Znaleziono: " + foundChainsCount + " ciągów bajtów w : " + fileCount + " plikach");
            }
        }
    }//GEN-LAST:event_findBytesChainButtonActionPerformed

    private void tabelaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaMouseClicked

    }//GEN-LAST:event_tabelaMouseClicked

    private void firstChainFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_firstChainFocusGained
        firstChain.setBackground(Color.WHITE);
    }//GEN-LAST:event_firstChainFocusGained

    private void secondChainFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_secondChainFocusGained
        secondChain.setBackground(Color.WHITE);
    }//GEN-LAST:event_secondChainFocusGained

    private void rozszerzenieFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rozszerzenieFocusGained
        rozszerzenie.setBackground(Color.WHITE);        // TODO add your handling code here:
    }//GEN-LAST:event_rozszerzenieFocusGained

    private void closeByteViewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeByteViewerActionPerformed
        ByteViewer.setVisible(false);
        bytesTextArea.setText("");
        viewedFileTextfield.setText("");// TODO add your handling code here:
    }//GEN-LAST:event_closeByteViewerActionPerformed

    private void replaceChainInOneFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceChainInOneFileButtonActionPerformed
        if (!isDataEmpty(firstChain.getText(), firstChain) && !isDataEmpty(secondChain.getText(), secondChain)) {
            if (isAByte(firstChain.getText(), firstChain) && isAByte(secondChain.getText(), secondChain)) {
                if (confirmationAlert()) {
                    byte[] searchedChain = getBytesFromString(firstChain.getText());
                    byte[] replaceChain = getBytesFromString(secondChain.getText());
                    try {
                        int row = tabela.getSelectedRow();
                        if (row >= 0) {
                            String path = (tabela.getModel().getValueAt(row, 1).toString());
                            if (isChainInFile(getBytesFromString(firstChain.getText()), path)) {
                                replaceChain(path, searchedChain, replaceChain);
                                JOptionPane.showMessageDialog(null, "Zamieniono ciągi bajtów");
                            } else {
                                JOptionPane.showMessageDialog(rootPane, "W wybranym pliku nie znaleziono wskazanego ciągu bajtów.");
                            }
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e);
                    }
                }
            }
        }

    }//GEN-LAST:event_replaceChainInOneFileButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        closeApplication();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FileModifier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FileModifier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FileModifier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FileModifier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FileModifier().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog ByteViewer;
    private javax.swing.JTextArea bytesTextArea;
    private javax.swing.JButton chooseFileButton;
    private javax.swing.JButton closeByteViewer;
    private javax.swing.JButton findBytesChainButton;
    private javax.swing.JTextField firstChain;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField pathFile;
    private javax.swing.JButton replaceBytesChainButton;
    private javax.swing.JButton replaceChainInOneFileButton;
    private javax.swing.JTextField rozszerzenie;
    private javax.swing.JTextField secondChain;
    private javax.swing.JButton showBytesChainButton;
    private javax.swing.JButton showFilesButton;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField viewedFileTextfield;
    // End of variables declaration//GEN-END:variables
}
