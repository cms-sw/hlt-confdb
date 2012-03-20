package confdb.converter;

public class Statistics 
{
	double valsum = 0;
	double val2sum = 0;
	int n = 0;
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;
	
	void add( long val )
	{
		valsum += val;
		val2sum += val * val;
		n += 1;
		if ( val < min )
			min = val;
		if ( val > max )
			max = val;
	}
	
	public double getMean()
	{
		if ( n == 0 )
			return 0;
		return valsum / n;
	}
	
	public int getN()
	{
		return n;
	}

	public double getMin()
	{
		return min;
	}
	
	public double getMax()
	{
		return max;
	}
	
	public double getSqrtVariance()
	{
		if ( n == 0 )
			return 0;
		double mean = getMean();
		double variance = val2sum - n * mean * mean;
        variance /= n;
        return Math.sqrt( variance );
	}

}
