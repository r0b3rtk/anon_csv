package nl.robertk;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Created by robert on 19-11-2016.
 */
public class CsvFileFilter extends FileFilter
{
    @Override
    public boolean accept(File f)
    {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
    }

    @Override
    public String getDescription()
    {
        return "Comma separated files (*.csv)";
    }
}
