package org.salp.jroutine.schedule.lb;

import org.salp.jroutine.schedule.lb.Instance;
import org.salp.jroutine.schedule.lb.RoundRobinLoadBalancer;
import org.salp.jroutine.schedule.lb.WeightRoundRobinLoadBalancer;

import junit.framework.Assert;
import junit.framework.TestCase;

public class LoadBalancerTest extends TestCase {

    private Instance[] instances;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        instances = new Instance[4];
        for (int i = 0; i < instances.length; i++) {
            instances[i] = new Server(i);
        }
    }

    public void testRoundRobin() {
        RoundRobinLoadBalancer balancer = new RoundRobinLoadBalancer();
        int[] hits = new int[instances.length];

        for (int i = 0; i < 100 * instances.length; i++) {
            Instance selected = balancer.select(instances);
            int index = indexOf(selected);
            hits[index] = hits[index] + 1;
        }

        Assert.assertEquals(hits[0], hits[1]);
        Assert.assertEquals(hits[0], hits[2]);
        Assert.assertEquals(hits[0], hits[3]);
    }

    public void testWeightRoundRobin() {
        WeightRoundRobinLoadBalancer balancer = new WeightRoundRobinLoadBalancer();
        int[] hits = new int[instances.length];

        for (int i = 0; i < 100 * instances.length; i++) {
            Instance selected = balancer.select(instances);
            int index = indexOf(selected);
            hits[index] = hits[index] + 1;
        }

        Assert.assertTrue(hits[0] < hits[1]);
        Assert.assertTrue(hits[1] < hits[2]);
        Assert.assertTrue(hits[2] < hits[3]);
    }

    private int indexOf(Instance selected) {
        for (int i = 0; i < instances.length; i++) {
            if (selected == instances[i]) {
                return i;
            }
        }
        return -1;
    }

    private class Server implements Instance {

        private int weight;
        private int currentWeight;

        public Server(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }

        public int getCurrentWeight() {
            return currentWeight;
        }

        public void setCurrentWeight(int currentWeight) {
            this.currentWeight = currentWeight;
        }

    }
}
