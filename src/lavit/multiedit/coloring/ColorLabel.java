package lavit.multiedit.coloring;

public final class ColorLabel implements Comparable<ColorLabel>
{
	private int _start;
	private int _length;
	private int _label;
	
	public ColorLabel(int start, int length, int label)
	{
		_start = start;
		_length = length;
		_label = label;
	}
	
	public int getStart()
	{
		return _start;
	}
	
	public int getEnd()
	{
		return _start + _length;
	}
	
	public int getLength()
	{
		return _length;
	}
	
	public int getLabel()
	{
		return _label;
	}
	
	public boolean contains(int pos)
	{
		return _start <= pos && pos < _start + _length;
	}
	
	public int compareTo(ColorLabel c)
	{
		if (_start != c._start)
			return _start - c._start;
		else if (_length != c._length)
			return _length - c._length;
		else
			return 0;
	}
}
