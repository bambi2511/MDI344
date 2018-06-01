# -*- encoding: utf-8 -*-
from bs4 import BeautifulSoup
import pandas as pd
from tasktimer import call_repeatedly
from urllib.request import urlopen
from urllib.parse import urlencode, unquote
import re


# fonction appelée périodiquement
def urlcall(toBeProcessed):
    if toBeProcessed["to_process"]:
        crawl_page(toBeProcessed["domain"],
                   toBeProcessed["to_process"],
                   toBeProcessed["processed"])
        print(toBeProcessed["to_process"])
        print(toBeProcessed["processed"])
        print("Not done yet")
        return False
    else:
        print("Done")
        return True


def getSearchResults(params, max_results):
    '''
    Return max_result first outputs
    '''
    patentLinks = set()
    for p in range(1, max_results):
        params['p'] = p
        searchPage = getHtml(query, params)
        tmp_set = getResultContents(searchPage)
        patentLinks = patentLinks.union(tmp_set)
    return patentLinks


def getHtml(html, params):
    params = urlencode(params)
    response = urlopen(html + "?" + params)
    return response


def getResultContents(page):
    '''
    input:
        page: html search contents (string)
    output:
        links: links matching regex (set)
    '''
    tmp_links = set()
    try:
        # soup = BeautifulSoup(page, 'html.parser')
        soup = BeautifulSoup(page, 'html5lib')
        tmp_tags = soup.find_all("div", attrs={"class": "legacy-container"})
        # tmp_tags = soup.find_all("table", attrs={"class": "listing_table"})
        tmp_links = set([a.get("href") for a in tmp_tags[0].find_all('a') if
                         a.get("href") is not None])
        return tmp_links
    except AttributeError as e:
        print(e)
        return tmp_links


def getPatentLinks(page):
    '''
    input:
        page: html patent contents (string)
    output:
        links: other patents linked to this patent (set)
    '''
    tmp_links = set()
    # pattern = re.compile(r'^/wiki/(?!.*redlink=1).*$', re.IGNORECASE)
    pattern = re.compile(r'.*[0-9]{7}.html$', re.IGNORECASE)
    try:
        soup = BeautifulSoup(page, 'html.parser')
        tmp_links = set([a.get("href") for a in soup.find_all('a') if
                         a.get("href") is not None])
        tmp_links = set([link for link in tmp_links if pattern.match(link)])
        return tmp_links
    except AttributeError as e:
        print(e)
        return tmp_links


def crawl_page(domain, to_process, processed):
    '''
    input:
        domain: root of the domain (string)
        to_process: pages that must be seen (set)
        processed: pages that were already seen (set)
    output:
       Finished: True/False
    '''
    input_link = to_process.pop()
    output_links = getPatentLinks(urlopen(domain + input_link))

    processed.add(input_link)
    to_process = to_process | (output_links - processed)

    return to_process is None


def getPatentContents(page):
    '''
    input:
        page: html patent contents (string)
    output:
        links: dictionary giving patents informations
    '''
    disp_elm_titles = set(
          ["Title:", "Inventors:", "Abstract:", "Application Number:",
           "Publication Date:", "Filing Date:", "Export Citation:",
           "Assignee:", "Primary Class:", "Other Classes:",
           "International Classes:"])

    tmp_dict = dict()
    # pattern = re.compile(r'^/wiki/(?!.*redlink=1).*$', re.IGNORECASE)
    try:
        soup = BeautifulSoup(page, 'html.parser')
        tmp_tags = soup.find_all("div", attrs={"class": "disp_doc2"})

        for tag in tmp_tags:
            key_tag = tag.find("div", attrs={"class": "disp_elm_title"})

            if (key_tag is not None):
                key = unquote(key_tag.text).strip()

                if key in disp_elm_titles:
                    value_tag = tag.find("div",
                                         attrs={"class": "disp_elm_text"})
                    if (value_tag is not None):
                        value = unquote(value_tag.text).strip()
                        tmp_dict[key] = value

        return tmp_dict

    except AttributeError as e:
        print(e)
        return tmp_dict


if __name__ == '__main__':

    domain = 'http://www.freepatentsonline.com'
    query = domain + '/result.html'
    params = {'sort': 'relevance',
              'srch': 'top',
              'query_txt': 'video',
              'patents': 'on'
              }
    max_results = 5
    outputFile = "../data/export.json"

    to_process = getSearchResults(params, max_results)
    processed = set()

    call_repeatedly(0.5, 60, urlcall, {"to_process": to_process,
                                       "processed": processed,
                                       "domain": domain})

    processed = list(map(lambda url: domain + url, processed))

    patent_contents = []
    for link in processed:
        patent_contents.append(getPatentContents(urlopen(link)))
    df_patents = pd.DataFrame(patent_contents)

    df_patents["Filing Date:"] = pd.to_datetime(df_patents["Filing Date:"],
                                                format="%m/%d/%Y")
    df_patents["Publication Date:"] = pd.to_datetime(
                                               df_patents["Publication Date:"],
                                               format="%m/%d/%Y")

    df_patents.to_json(path_or_buf=outputFile, orient='records')
    print(df_patents)
