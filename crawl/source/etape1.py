# -*- encoding: utf-8 -*-
from tasktimer import call_repeatedly
from urllib.request import urlopen
from urllib.parse import urlencode
import re


def getHtml(html, params):
    params = urlencode(params)
    response = urlopen(html + "?" + params)
    return response


html = 'http://www.freepatentsonline.com/result.html'
params = {'sort': 'relevance',
          'srch': 'top',
          'query_txt': 'video',
          'submit': '',
          'patents': 'on'
          }
outputFile = "../data/search.html"


response = getHtml(html, params)

with open(outputFile, mode='w') as file:
    file.write(response.read().decode('utf-8'))
