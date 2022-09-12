package org.valkyrienskies.addon.control.util;

import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;

public interface TileEntityEnv extends Environment {


    @Override
    default void onConnect(Node node) {}
    @Override
    default void onDisconnect(Node node) {}

    @Override
    default void onMessage(Message message) {}
}
