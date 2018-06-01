# -*- encoding: utf-8 -*-
# test access to the ISTEX API
#  author: JC Moissinac (c) 2016 inspired by J.F.Sebastian

from threading import Event, Thread
from functools import partial


def call_repeatedly(interval, max_iter, func, *args):
    stopped = Event()

    def loop(max_iter):
        internalstop = False
        while (not stopped.wait(interval)) and (not internalstop) and \
              (max_iter > 0):
            # the first call is in `interval` secs
            internalstop = func(*args)
            max_iter = max_iter - 1
    thread = Thread(target=partial(loop, max_iter=max_iter))
    thread.start()
    thread.join()
    return stopped.set

# The event is used to stop the repetitions:
# cancel_future_calls = call_repeatedly(5, print, "Hello, World")
# do something else here...
# cancel_future_calls() # stop future calls
