#!/usr/bin/python3
# -*- coding: utf-8 -*-

# Ne pas se soucier de ces imports
import setpath
from flask import Flask, render_template, session, request, redirect, flash, \
                  url_for
from getpage import getPage

app = Flask(__name__)

app.secret_key = "dev"


@app.route('/', methods=('GET', 'POST'))
def index():
    if request.method == 'POST':
        topic = request.form['topic']

        page, links, error = testTopic(topic)

        if error is None:
            session.clear()
            session['article'] = page
            session['counter'] = 0
            print(session)

            return redirect(url_for('results', topic=page))

        flash(error)
    # return render_template(url_for('index'))
    return render_template('index.html')


@app.route('/results/<topic>', methods=['GET', 'POST'])
def results(topic=None):

    if request.args.get('destination', None):
        topic = request.args['destination']
        print("POST", topic)
        return redirect(url_for('results', topic=topic))
    else:
        page, links, error = testTopic(topic)
        if error is None:
            print("topic exists")
            session['counter'] = session['counter'] + 1
            print(session)
            print('GET')
            if testWin(page):
                flash("Vous avez gagné en {} coups".format(session["counter"]))
                return redirect(url_for('index'))

            return render_template('results.html', links=links)

        flash(error)
        return redirect(url_for('index'))
        # return render_template('results.html')


def testTopic(topic, verbose=False):
    page, links = getPage(topic)
    error = None

    if links is None:
        if verbose:
            print("topic does not exist")
        error = 'Le sujet {} n\'est pas traité dans Wikipedia'.format(topic)
    elif len(links) == 0:
        if verbose:
            print("no link")
        error = 'Pas de lien de trouvé dans la page {}'.format(page)
    else:
        if verbose:
            print("topic exists")
    return page, links, error


def testWin(page, pattern='Philosophie', verbose=False):
    if page == pattern:
        return True
    else:
        return False


if __name__ == '__main__':
    app.run(debug=True)
