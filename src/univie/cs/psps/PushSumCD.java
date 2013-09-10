/*
 * Copyright (c) 2013 Faculty of Computer Science, University of Vienna
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package univie.cs.psps;

import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.vector.VectControl;
import univie.cs.psps.utils.AggregationProtocol;
import univie.cs.psps.utils.ProtocolUtils;

/**
 * A cycle-driven implementation of the Push-Sum protocol.
 * <p>
 * In each cycle a node sends half of it's value and half of it's weight to a
 * randomly selected neighbor and to itself. In the cycle driven implementation,
 * instead of sending a message we write directly into the buffers of the
 * receiving node.
 * <p>
 * Afterwards all nodes sum up the received values. Since this has to happen
 * after all the values have been exchanged, we implement this as a seperate
 * control class {@link PushSumCDUpdate}, that is executed after each cycle.
 * 
 * @author Dario Seidl
 * 
 */
public class PushSumCD implements AggregationProtocol, CDProtocol
{
	private double trueValue;
	private double value;
	private double weight;

	private double valueBuffer;
	private double weightBuffer;

	/**
	 * The standard constructor called by the simulator, reading parameters from
	 * the configuration file.
	 * 
	 * @param prefix
	 *            the prefix for this control in the configuration file.
	 */
	public PushSumCD(String prefix)
	{}

	/**
	 * In each cycle, writes half value and half weight into the buffer of
	 * itself and a randomly selected neighbor.
	 */
	@Override
	public void nextCycle(Node self, int protocolID)
	{
		Node neighbor = ProtocolUtils.getRandomNeighbor(self, protocolID);

		if (neighbor != null)
		{
			PushSumCD neighborProtocol = (PushSumCD) neighbor.getProtocol(protocolID);

			// send half of value and weight to self
			valueBuffer += value / 2;
			weightBuffer += weight / 2;

			// send half of value and weight to a random neighbor
			neighborProtocol.valueBuffer += value / 2;
			neighborProtocol.weightBuffer += weight / 2;
		}
	}

	/**
	 * Update function called by {@link PushSumCDUpdate}. Sets value and weight
	 * to the sum of the received values and weights stored in the buffers, then
	 * empties the buffers.
	 */
	public void update()
	{
		// sum up
		value = valueBuffer;
		weight = weightBuffer;
		valueBuffer = 0.;
		weightBuffer = 0.;
	}

	/**
	 * Returns the unmodified value of the node.
	 */
	@Override
	public double getTrueValue()
	{
		return trueValue;
	}

	/**
	 * Returns the local estimate of the mean value of all nodes in the network.
	 * The estimate is obtained by dividing value by weight.
	 */
	@Override
	public double getEstimate()
	{
		return value / weight;
	}

	/**
	 * Sets the value of this node. Should be called by subclasses of
	 * {@link VectControl} to initialize all nodes in the network.
	 */
	public void initializeValue(double value)
	{
		this.trueValue = value;
		this.value = value;
		valueBuffer = value;
	}

	/**
	 * Sets the weight of this node. Should be called by subclasses of
	 * {@link VectControl} to initialize all nodes in the network.
	 */
	public void initializeWeight(double weight)
	{
		this.weight = weight;
		weightBuffer = weight;
	}

	@Override
	public String toString()
	{
		return String.format("(%e, %e)", value, weight);
	}

	@Override
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
	}
}