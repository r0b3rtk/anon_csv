package nl.robertk.anoncsv;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App
{
    private static CheckboxList cbl;
    private static JButton writeButton;
    private static final List<String> columnsToAnonymize = new ArrayList<String>();
    private static File csvFileToAnonymize;
    private static JLabel selectedFileLabel;

    public static void main(String[] args)
    {
        final JFrame frame = new JFrame("Anonymize CSV file");

        frame.setSize(new Dimension(640, 480));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        BorderLayout manager = new BorderLayout();
        manager.setVgap(30);
        frame.setLayout(manager);
        JButton browseButton = new JButton("Browse...");

        browseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                final JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new CsvFileFilter());
                int returnVal = fc.showOpenDialog(frame);

                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    selectedFileLabel.setText("Selected file: " + fc.getSelectedFile().getAbsolutePath());
                    csvFileToAnonymize = fc.getSelectedFile();
                    try
                    {
                        cbl.clearCheckboxes();
                        String[] csvHeader = getCSVHeader(csvFileToAnonymize);
                        for (final String s : csvHeader)
                        {
                            final JCheckBox cb = new JCheckBox(s);
                            cb.addChangeListener(new ChangeListener()
                            {
                                public void stateChanged(ChangeEvent e)
                                {
                                    if (cb.isSelected())
                                    {
                                        columnsToAnonymize.add(s);
                                    } else
                                    {
                                        columnsToAnonymize.remove(s);
                                    }
                                }
                            });
                            cbl.addCheckbox(cb);
                        }

                        writeButton.setEnabled(true);

                    } catch (IOException e1)
                    {
                        e1.printStackTrace();
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }

                }
            }
        });

        BorderLayout layout = new BorderLayout();
        layout.setHgap(10);
        layout.setVgap(10);
        JPanel browsePanel = new JPanel(layout);
        browsePanel.add(new JLabel("Step 1: select CSV file to anonymize"), BorderLayout.PAGE_START);
        browsePanel.add(browseButton, BorderLayout.LINE_START);
        selectedFileLabel = new JLabel("No file selected");
        browsePanel.add(selectedFileLabel, BorderLayout.CENTER);
        frame.add(browsePanel, BorderLayout.PAGE_START);

        JPanel checkboxPanel = new JPanel(new BorderLayout());
        JLabel checkBoxesLabel = new JLabel("Step 2: Select the columns to anonymize:");
        checkboxPanel.add(checkBoxesLabel, BorderLayout.PAGE_START);

        cbl = new CheckboxList();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(cbl);
        checkboxPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(checkboxPanel, BorderLayout.CENTER);

        writeButton = new JButton("Step 3: Write anonymized CSV file");
        writeButton.setEnabled(false);
        writeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    anonymizeCSV(csvFileToAnonymize);
                } catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
        });

        frame.add(writeButton, BorderLayout.PAGE_END);
        frame.setVisible(true);

    }

    private static String[] getCSVHeader(File csvFile) throws Exception
    {
        ICsvMapReader mapReader = null;
        try
        {
            mapReader = new CsvMapReader(new FileReader(csvFile), CsvPreference.STANDARD_PREFERENCE);
            final String[] header = mapReader.getHeader(true);
            return header;
        } finally
        {
            if (mapReader != null)
            {
                mapReader.close();
            }
        }
    }


    private static void anonymizeCSV(File csvFile) throws Exception
    {
        ICsvMapReader mapReader = null;
        ICsvMapWriter mapWriter = null;
        try
        {
            mapReader = new CsvMapReader(new FileReader(csvFile), CsvPreference.STANDARD_PREFERENCE);

            String newFileName = csvFile.getAbsolutePath() + "_anonymized.csv";
            File f = new File(newFileName);
            if (f.exists())
            {
                if (JOptionPane.showConfirmDialog(new JFrame(),
                        "File " + newFileName + " already exists, overwrite?", "File exists",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                    return;
            }

            mapWriter = new CsvMapWriter(new FileWriter(newFileName),
                    CsvPreference.STANDARD_PREFERENCE);

            final String[] header = mapReader.getHeader(true);

            CellProcessor[] cellProcessors = new CellProcessor[header.length];

            for (int i = 0; i < header.length; i++)
            {
                cellProcessors[i] = null;
            }

            mapWriter.writeHeader(header);

            Map<String, Object> customerMap;
            while ((customerMap = mapReader.read(header, cellProcessors)) != null)
            {
                for (String s : columnsToAnonymize)
                {
                    customerMap.put(s, "anonymized");
                }

                mapWriter.write(customerMap, header, cellProcessors);

            }

            JOptionPane.showMessageDialog(new JFrame(), "File has been anonymized and saved to " + newFileName);
        } finally
        {
            if (mapReader != null)
            {
                mapReader.close();
            }
            if (mapWriter != null)
            {
                mapWriter.close();
            }

        }
    }

}
