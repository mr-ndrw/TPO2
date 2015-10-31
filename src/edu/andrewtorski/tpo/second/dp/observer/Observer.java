package edu.andrewtorski.tpo.second.dp.observer;

import java.io.IOException;

/**
 * Created by andrew on 19.10.2015.
 */
public interface Observer {
    Subject getSubject();

    Observer update(String message) throws IOException;
}
