package lavit.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>開いたファイルの履歴を管理します。</p>
 * @author Yuuki SHINOBU
 */
public class FileHistory
{
	/**
	 * <p>ファイル履歴を保持する数の上限値の既定値です。</p>
	 */
	public static final int DEFAULT_LIMIT = 8;

	private int _limit = DEFAULT_LIMIT;
	private LinkedList<File> _files = new LinkedList<File>();

	/**
	 * <p>このファイル履歴オブジェクトに設定されているファイルリストの要素数の上限値を取得します。</p>
	 * @return 設定されている上限値
	 */
	public int getLimit()
	{
		return _limit;
	}

	/**
	 * <p>このファイル履歴オブジェクトが含むファイルリストの要素数の上限値を設定します。</p>
	 * @param limit 設定する上限値
	 */
	public void setLimit(int limit)
	{
		_limit = limit;
		trimSize();
	}

	/**
	 * <p>ファイル履歴をすべて削除します。</p>
	 */
	public void clear()
	{
		_files.clear();
	}

	/**
	 * <p>ファイルを履歴に追加します。</p>
	 * <p>このファイルが既に含まれる場合、その要素を先頭へ移動します。</p>
	 * @param file 追加するファイル
	 */
	public void add(File file)
	{
		int index = _files.indexOf(file);

		if (index != -1)
		{
			_files.remove(index);
		}

		_files.addFirst(file);
		trimSize();
	}

	/**
	 * <p>ファイル履歴のリストを読み取り専用として取得します。</p>
	 * @return ファイル履歴のリスト
	 */
	public List<File> getFiles()
	{
		return Collections.unmodifiableList(_files);
	}

	/**
	 * <p>ファイル履歴のリストをファイルに保存します。</p>
	 * @param fileName 保存するファイル名
	 */
	public void save(String fileName)
	{
		try
		{
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileName))));

			for (File file : _files)
			{
				writer.println(file.getAbsolutePath());
			}
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * <p>ファイルからファイル履歴のリストを読み込みます。</p>
	 * @param fileName 読み込むファイル名
	 * @return ファイル履歴オブジェクト
	 */
	public static FileHistory fromFile(String fileName)
	{
		File file = new File(fileName);

		FileHistory history = new FileHistory();

		if (file.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file)));

				String line;
				try
				{
					while ((line = reader.readLine()) != null)
					{
						history.add(new File(line));
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					reader.close();
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return history;
	}

	/**
	 * <p>リストの要素数を設定された上限値以下に切り詰めます。</p>
	 */
	private void trimSize()
	{
		if (_files.size() > _limit)
		{
			if (_files.size() == _limit + 1)
			{
				_files.remove(_files.size() - 1);
			}
			else
			{
				_files = (LinkedList<File>) _files.subList(0, _limit - 1);
			}
		}
	}
}
