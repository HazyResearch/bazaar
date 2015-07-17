(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){

var Help = React.createClass({displayName: "Help",


  render: function() {
    var show = this.props.isHelp

    var wrapperStyle = {position:'fixed', top: '0px', right:0, minHeight:'100%', overflowX:'hidden', transition:'width .25s',
                               WebkitTransition:'width .25s', backgroundColor: 'rgb(71, 71, 71)'}
    var columnStyle = {position:'absolute', top:'50px', paddingTop:'10px', paddingBottom:'10px', paddingLeft:'10px', paddingRight:'10px',
                                      minHeight:'100%', width:'280px', color:'white', zIndex:3}

    var columnStyleBackground = {} //{position:'fixed', boxSizing:'borderBox', MozBoxSizing:'border-box', WebkitBoxSizing:'border-box',
                    //top:0, right:0, minHeight:'100%', backgroundColor:'rgba(71,71,71,1)', transition:'width .25s', WebkitTransition:'width .25s',
                    //zIndex:1}
    if (show) {
        columnStyleBackground.width = '300px'
        wrapperStyle.width = '300px'
    } else {
        wrapperStyle.width = '0px'
        columnStyleBackground.width = '0px';
    }

    return (React.createElement("div", {style: wrapperStyle}, 
               React.createElement("div", {className: "help", style: columnStyle}, 
                React.createElement("h1", null, "Query Examples"), 

                React.createElement("h3", null, "Words and Phrases"), 
                React.createElement("code", null, "quick"), " and ", React.createElement("code", null, "\"quick brown\""), 

                React.createElement("h3", null, "Field names"), 
                React.createElement("code", null, "_id:4325235"), React.createElement("br", null), 
                React.createElement("code", null, "title:(quick OR brown)"), React.createElement("br", null), 
                React.createElement("code", null, "book.\\*:(quick brown)"), React.createElement("br", null), 
                React.createElement("code", null, "_missing_:title"), React.createElement("br", null), 
                React.createElement("code", null, "_exists_:title"), 

                React.createElement("h3", null, "Wildcards"), 
                React.createElement("code", null, "qu?ck bro*"), 

                React.createElement("h3", null, "Regular Expressions"), 
                React.createElement("code", null, "name:/joh?n(ath[oa]n)/"), 

                React.createElement("h3", null, "Fuzziness"), 
                React.createElement("code", null, "quikc~ brwn~ foks~"), React.createElement("br", null), 
                React.createElement("code", null, "quikc~1"), 

                React.createElement("h3", null, "Proximity Searches"), 
                React.createElement("code", null, "\"fox quick\"~5"), 

                React.createElement("h3", null, "Ranges"), 
                React.createElement("code", null, "date:[2012-01-01 TO 2012-12-31]"), React.createElement("br", null), 
                React.createElement("code", null, "count:[1 TO 5]"), React.createElement("br", null), 
                React.createElement("code", null, "tag: ", "{", "alpha TO omega", "}"), React.createElement("br", null), 
                React.createElement("code", null, "count:[10 TO *]"), React.createElement("br", null), 
                React.createElement("code", null, "date:", "{", "* TO 2012-01-01", "}"), React.createElement("br", null), 
                React.createElement("code", null, "count:[1 TO 5", "}"), React.createElement("br", null), 
                React.createElement("code", null, "age:>=10"), React.createElement("br", null), 
                React.createElement("code", null, "age:(>=10 AND <20)"), 

                React.createElement("h3", null, "Boosting"), 
                React.createElement("code", null, "quick^2 fox"), React.createElement("br", null), 
                React.createElement("code", null, "\"john smith\"^2"), React.createElement("br", null), 
                React.createElement("code", null, "(foo bar)^4"), 

                React.createElement("h3", null, "Boolean Operators"), 
                React.createElement("code", null, "quick brown +fox -news"), React.createElement("br", null), 
                React.createElement("code", null, "((quick AND fox) OR (brown AND fox) OR fox) AND NOT news"), 

                React.createElement("h3", null, "Grouping"), 
                React.createElement("code", null, "(quick OR brown) AND fox"), React.createElement("br", null), 
                React.createElement("code", null, "status:(active OR pending) title:(full text search)^2"), 

                React.createElement("h3", null, "Reserved Characters"), 
                "Escape with backslash", React.createElement("br", null), 
                "Example: ", React.createElement("code", null, "\\(1\\+1\\)\\=2"), " , finds (1+1)=2 ", React.createElement("br", null), 
                "Characters: ", React.createElement("code", null, "+ - = && || > < ! ( ) ", "{", " ", "}", " [ ] ^ \" ~ * ? : \\ /"), 

                React.createElement("h3", null, "Empty Query"), 
                "Shows all results.", 

                React.createElement("p", null, 
                  "For more details, see ", React.createElement("a", {href: "https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax", target: "_blank"}, "here"), "."
                )
                  ), 
                  React.createElement("div", {style: columnStyleBackground}, 
                    React.createElement("div", {style: {position:'absolute', borderLeft:'1px solid white', minHeight:'100%', width:'1px'}})
                  )
            	))
  }
})

module.exports = Help

},{}],2:[function(require,module,exports){
/**
 *
 */
"use strict";

var TextWithAnnotations = require('./vis/TextWithAnnotations.js')
var AnnotationsSelector = require('./vis/AnnotationsSelector.js')
var Help = require('./help/Help.js')


var SearchPage = React.createClass({displayName: "SearchPage",
  notify: function(msg) {

  },
  handleKeywordQuery: function(keywords) {
    var facets = []
    $.each(this.state.extractors, function(index, value) {
      if (value.active)
        facets.push(value.name); });
    $.ajax({
      url: 'docs?keywords=' + encodeURIComponent(keywords) + 
            '&facets=' + facets.join(),
      success: function(data) {
        this.setState({data: data, keywords:keywords});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  handleShowMore: function() {
    var start = this.state.data.hits.length
    var facets = []
    $.each(this.state.extractors, function(index, value) {
      if (value.active)
        facets.push(value.name); });
    $.ajax({
      url: 'docs?start=' + start + '&keywords=' + encodeURIComponent(this.state.keywords) + 
            '&facets=' + facets.join(),
      success: function(data) {
        var all = { 'total': data.total, 'hits': this.state.data.hits.concat(data.hits) }
        this.setState({data: all, keywords:this.state.keywords});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });    
  },
  handleFacetChange: function(name, active) {
    $.each(this.state.extractors, function(index, value) {
      if (value.name == name)
        value.active = active; });
    this.handleKeywordQuery(this.state.keywords);
  },
  handleLoadExtractors: function() {
    $.ajax({
      url: 'annotators',
      success: function(data) {
        // add a field to represent active/non-active
        var extractors = data.map(function(it) {
          return {
            'name': it._source.name,
            'active': false
          };
        })
        if (this.isMounted()) {
          this.setState({extractors:extractors}); 
        }
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  handleToggleHelp: function() {
    this.setState({isHelp: !this.state.isHelp})
  },
  getInitialState: function() {
    return {
        data: {hits:[]},
        extractors: [],
        keywords: '',
        isHelp: false
    }
  },
  componentDidMount: function() {
    this.handleLoadExtractors()
    this.handleKeywordQuery('')
  },
  render: function() {
    return (
      React.createElement("div", null, 
        React.createElement(Header, {onKeywordQuery: this.handleKeywordQuery, 
          onToggleHelp: this.handleToggleHelp}), 
        React.createElement(NotificationBox, null), 
        React.createElement(Content, {style: {'height':'100%'}, 
          data: this.state.data, 
          extractors: this.state.extractors, 
          isHelp: this.state.isHelp, 
          onFacetChange: this.handleFacetChange, 
          onShowMore: this.handleShowMore})
      )
    );
  }
});

var Header = React.createClass({displayName: "Header",
  setFocus: function() {
    React.findDOMNode(this.refs.query).focus();
  },
  componentDidMount: function() {
    this.setFocus();
  },
  getInitialState: function() {
    return {query: ''};
  },
  inputSubmit: function() {
    var val = this.state.query;
    this.props.onKeywordQuery(val)
  },
  handleChange: function(evt) {
    this.setState({query: evt.target.value});
  },
  handleKeyDown: function(evt) {
      if (evt.keyCode == 13 ) {
          return this.inputSubmit();
      }
  },
  handleToggleHelp: function(evt) {
    this.props.onToggleHelp()
  },
  render: function() {
    return (
      React.createElement("div", {className: "header unselectable"}, 
        React.createElement("div", {style: {position:'absolute', top:0, left:0, marginLeft:'10px', marginTop:'10px', cursor:'pointer'}}, 
          React.createElement("span", {style: {fontFamily:'Open Sans, sans-serif', fontSize:'22px'}})
        ), 
        React.createElement("div", {style: {position:'absolute', top:0, left:200}}, 
          React.createElement("input", {type: "text", ref: "query", value: this.state.query, 
            onChange: this.handleChange, onKeyDown: this.handleKeyDown})
        ), 
        React.createElement("div", {style: {position:'absolute', right:0, width:60, paddingTop:'7px'}}, 
            React.createElement("div", {onClick: this.handleToggleHelp, style: {cursor:'pointer',width:'36px',height:'36px',borderRadius:'21px',border:'2px solid #CCC',color:'#CCC',fontSize:'30px',fontWeight:'bold',fontFamily:'courier',textAlign:'center'}}, "?")
        )

      )
    );
  }
});

var NotificationBox = React.createClass({displayName: "NotificationBox",
  render: function() {
    return (React.createElement("div", null));
  }
})

var Content = React.createClass({displayName: "Content",
  render: function() {
    return (
      React.createElement("div", {className: "content"}, 
        React.createElement(LeftMenu, {extractors: this.props.extractors, 
          onFacetChange: this.props.onFacetChange}), 
        React.createElement(Results, {data: this.props.data, onShowMore: this.props.onShowMore}), 
        React.createElement(Help, {isHelp: this.props.isHelp})
      )
      );
  }
})

var LeftMenu = React.createClass({displayName: "LeftMenu",
  render: function() {
    var onFacetChange = this.props.onFacetChange;
    var extractorNodes = this.props.extractors.map(function(ex) {
      return (
        React.createElement(Facet, {data: ex, onFacetChange: onFacetChange})
        );
    });
    return (React.createElement("div", {className: "leftmenu"}, React.createElement("br", null), 
      extractorNodes
      ));
  }
})

var Facet = React.createClass({displayName: "Facet",
  handleClick: function() {
    var active = !this.props.data.active;
    this.props.onFacetChange(this.props.data.name, active);
  },
  render: function() {
    var classes = 'facet';
    if (!this.props.data.active)
      classes += ' facet-inactive';
    return (React.createElement("div", {className: classes, onClick: this.handleClick}, 
       React.createElement("div", {style: {display:'inline-block',width:'30px'}}, 
         React.createElement("i", {className: "fa fa-check"})
       ), 
       this.props.data.name))
  }
})

var Results = React.createClass({displayName: "Results",
  handleShowMoreClick: function() {
    this.props.onShowMore();
  },

  render: function() {
    var resultNodes = this.props.data.hits.map(function(result) {
       return (
         React.createElement(Result, {data: result})
         );
     });
    var showMoreButton = ''
    if (this.props.data.hits.length > 0 && this.props.data.hits.length < this.props.data.total) {
       showMoreButton = (React.createElement("div", {style: {cursor:'pointer'}, onClick: this.handleShowMoreClick}, "Show more"))
     }

     return (React.createElement("div", {style: {marginLeft:'200px', marginRight:'200px'}}, 
       React.createElement("div", {style: {'textAlign':'right', paddingTop:'10px', paddingBottom:'5px', 'color':'#AAA'}}, 
         this.props.data.total, " results"
       ), 
       resultNodes, 
       showMoreButton
       ));
  }
})

var Result = React.createClass({displayName: "Result",
  getInitialState: function() {
   return {layers: [
     { name: "Extractors", active: true },
     { name: "Tokens", active: false },
     { name: "Sentences", active: false },
     { name: "Dependencies", active: false },
     { name: "Lemmas", active: false },
     { name: "PartOfSpeech", active: false },
     { name: "Details", active: false },
     ]};
  },
  onLayerChange: function(name, active) {
    $.each(this.state.layers, function(index, value) {
      if (value.name == name)
        value.active = active; 
    })
    if (this.isMounted()) {
      this.setState({layers:this.state.layers}); 
    }
  },
  render: function() {
    return (React.createElement("div", {className: "result"}, 
         React.createElement(TextWithAnnotations, {data: this.props.data, layers: this.state.layers}), 
         React.createElement(AnnotationsSelector, {layers: this.state.layers, onLayerChange: this.onLayerChange})
      ));
  }  
})



React.render(
  React.createElement(SearchPage, null),
  document.getElementById('page')
);

},{"./help/Help.js":1,"./vis/AnnotationsSelector.js":3,"./vis/TextWithAnnotations.js":4}],3:[function(require,module,exports){

var AnnotationsSelector = React.createClass({displayName: "AnnotationsSelector",

	render: function() {
		var onLayerChange = this.props.onLayerChange

		var buttons = this.props.layers.map(function(result) {
       		return (
         		React.createElement(AnnotationsSelectorButton, {data: result, 
         			onLayerChange: onLayerChange})
         	);
     	});
     	return (React.createElement("div", {className: "annotationsSelector"}, buttons));
	}
});

var AnnotationsSelectorButton = React.createClass({displayName: "AnnotationsSelectorButton",
  handleClick: function() {
    var active = !this.props.data.active;
    this.props.onLayerChange(this.props.data.name, active);
  },
  render: function() {
    var classes = 'facet';
    if (!this.props.data.active)
      classes += ' facet-inactive';
    return (React.createElement("div", {style: {fontSize:'10pt'}, className: classes, onClick: this.handleClick}, 
       React.createElement("div", {style: {display:'inline-block',width:'30px'}}, 
         React.createElement("i", {className: "fa fa-check"})
       ), this.props.data.name
     ))
  }
})

module.exports = AnnotationsSelector
},{}],4:[function(require,module,exports){
var SpansVisualization = require('./core/SpansVisualization.js')
var TokenTagsVisualization = require('./core/TokenTagsVisualization.js')
var EdgesVisualization = require('./core/EdgesVisualization.js')

var TokensVisualization = function(element, source) {
	return SpansVisualization(element, source.tokenOffsets)
}

var SentencesVisualization = function(element, source) {
	return SpansVisualization(element, source.sentenceOffsets)
}

var PartOfSpeechVisualization = function(element, source) {
	return TokenTagsVisualization(element, source.tokenOffsets, source.poss)
}

var LemmasVisualization = function(element, source) {
	return TokenTagsVisualization(element, source.tokenOffsets, source.lemmas)
}

var DependenciesVisualization = function(element, source) {
	return EdgesVisualization(element, source.tokenOffsets, source.sentenceOffsets, source.sentenceTokenOffsets, source.sentenceDependencies)
}

var ExtractorsVisualization = function(element, source, annotations) {
   var sentenceTokenOffsets = source['sentenceTokenOffsets']
   var tokenOffsets = source['tokenOffsets']
   var extractorOffsets = []

   $.each(annotations, function(i, a) {
   	  var sentNum = a.range.sentNum
   	  var sentenceBeginToken = sentenceTokenOffsets[sentNum][0]
   	  var tokenFrom = sentenceBeginToken + a.range.f
   	  var tokenTo = sentenceBeginToken + a.range.t
      var charFrom = tokenOffsets[tokenFrom][0]
      var charTo = tokenOffsets[tokenTo - 1][1]
      extractorOffsets.push([charFrom,charTo])
   })
   return SpansVisualization(element, extractorOffsets)
}

var TextWithAnnotations = React.createClass({displayName: "TextWithAnnotations",

  componentDidMount: function() {
  	this.vis = {}
  	this.buildCustomDom()
  },
  componentDidUpdate: function() {
  	this.buildCustomDom()
  },
  buildCustomDom: function() {
    var div = React.findDOMNode(this)
    //cleanup existing visualizations
    $.each(this.vis, function(k,v) { v.destroy() })

  	this.vis = {}

    var annotations = this.props.data.annotations
    var sourceData = this.props.data._source
    var vis = this.vis

    $.each(this.props.layers, function(i, l) {
        if (vis && vis[l.name] && !l.active) {
        	vis[l.name].destroy()
        	delete vis[l.name]
        }
        if (vis && !vis[l.name] && l.active) {
        	if (l.name == 'Tokens')
        		vis[l.name] = new TokensVisualization(div, sourceData)
        	if (l.name == 'Sentences')
        		vis[l.name] = new SentencesVisualization(div, sourceData)
        	if (l.name == 'Extractors')
        		vis[l.name] = new ExtractorsVisualization(div, sourceData, annotations)
        	if (l.name == 'Dependencies')
        		vis[l.name] = new DependenciesVisualization(div, sourceData)
        	if (l.name == 'Lemmas')
        		vis[l.name] = new LemmasVisualization(div, sourceData)        		
        	if (l.name == 'PartOfSpeech')
        		vis[l.name] = new PartOfSpeechVisualization(div, sourceData)        		
        }
    })
  },
  isActive: function(name) {
  	var isActive = false
    $.each(this.props.layers, function(i, l) {
       if (l.name == name) { isActive = l.active; return false }
    })
    return isActive
  },

  render: function() {
    content = this.props.data._source.content;
    // if we have field with keyword highlighting, take that
    if (this.props.data.highlight != null &&
        this.props.data.highlight.content != null) {
      content = this.props.data.highlight.content[0];
    }
    var details = []
    if (this.isActive('Details')) {
	    $.each(this.props.data.annotations, function(i, value) {
	    	details.push(React.createElement("div", {className: "extractionBlue"}, JSON.stringify(value), " "));
	    })
	    $.each(this.props.data._source, function(name, value) {
	      if (name != 'content' && name != 'id')
	        details.push (React.createElement("div", {className: "extraction"}, name, " : ", JSON.stringify(value), " "));
	    })
	}

    var div = (React.createElement("div", null, React.createElement("span", {dangerouslySetInnerHTML: {__html: content}}), 
        React.createElement("br", null), React.createElement("div", {style: {'color':'green'}}, this.props.data._id), 
        details
    	))

    return div;
  }
});

module.exports = TextWithAnnotations


},{"./core/EdgesVisualization.js":6,"./core/SpansVisualization.js":7,"./core/TokenTagsVisualization.js":8}],5:[function(require,module,exports){
var CharOffsets = (function() {
	var ELEMENT = 1;
	var TEXT = 3;
	
	var offsetComparator = function(e1, e2) {
		return e1.readrOffset - e2.readrOffset;					
	};
		
	var indexOffsets = function(node, offset) {
		node.readrOffset = offset;
		if (node.nodeType == TEXT) {
			node.readrLength = node.nodeValue.length;
		} else if (node.nodeType == ELEMENT) {
			// ignore if has class ignoreReadrLength
			if (goog.dom.classes.has(node, 'ignoreReadrLength')) {
				node.readrLength = 0;
			} else {
				// sum up lengths of children
				var l = 0;
				for (var i=0, ii = node.childNodes.length; i < ii; i++) {
					var child = node.childNodes[i];
					indexOffsets(child, offset + l);
					l += child.readrLength;
				}
				node.readrLength = l;
			}
		}
	};
	
	var getTextRangesToHighlightFromIndex = function(node, start, end) {
		var results = new Array();
		recur(node, start, end, results);
		return results;
	};
	
	var recur = function(node, start, end, results) {
		if (end - start <= 0) return;
	
		// we assume that start >= node.readrOffset and end <= node.readrOffset + node.readrLength
		if (node.nodeType == TEXT) {
			results.push([node, start - node.readrOffset, end - node.readrOffset, start, end]);
			return;
		}
		// binary search for start and end
		var ns = goog.array.binarySearch(node.childNodes, { readrOffset : start }, offsetComparator);
		var ne = goog.array.binarySearch(node.childNodes, { readrOffset : end }, offsetComparator);
		
		if (ns < 0) { ns = -ns-2; }
		if (ne < 0) { ne = -ne-1; }
		
		for (var i=ns; i < ne; i++) {
			var child = node.childNodes[i];
			var s = (i==ns)? start : child.readrOffset;
			var e = (i==ne-1)? end : child.readrOffset + child.readrLength;
			
			recur(child, s, e, results);
		}
	};
	
	var createMultiRangeSpans = function(element, tokenOffsets, renderedSpans, documentOffset) {
		if (!renderedSpans)
			renderedSpans = new Array();
		if (!documentOffset)
			documentOffset = 0
		indexOffsets(element[0], documentOffset)
		for (var j=0, jj = tokenOffsets.length; j < jj; j++) {
			// token has offsets t.f, t.t
			var rs = createSingleRangeSpans(element, tokenOffsets[j]);
			renderedSpans.push(rs);
		}
		return renderedSpans;
	};

    var FROM = 0
    var TO = 1
	
	// example tokenOffset: { f:12, t:23 }
	var createSingleRangeSpans = function(element, tokenOffset) {
		//if (!documentOffset) 
			//documentOffset = 0
		var sels = new Array();
		var todo = getTextRangesToHighlightFromIndex
			(element[0], tokenOffset[FROM], tokenOffset[TO]);
		for (var i=0, ii = todo.length; i < ii; i++) {
			var t = todo[i];
			var range = goog.dom.Range.createFromNodes(t[0], t[1], t[0], t[2]);
			var el = goog.dom.createDom('span'); //, { 'style':'background-color:green'}); 
			range.surroundContents(el);
			indexOffsets(t[0].parentNode, t[0].parentNode.readrOffset);
			sels.push(el);
		}
		return { sels:sels };
	};
	
	//note, the output of this function is a singleton
	return {
		indexOffsets: indexOffsets,
		getTextRangesToHighlightFromIndex: getTextRangesToHighlightFromIndex,
		createMultiRangeSpans: createMultiRangeSpans,
		createSingleRangeSpans: createSingleRangeSpans
	};
})()

module.exports = CharOffsets
},{}],6:[function(require,module,exports){
var CharOffsets = require('./CharOffsets.js')

var is_chrome = navigator.userAgent.indexOf('Chrome') > -1;
var is_explorer = navigator.userAgent.indexOf('MSIE') > -1;
var is_firefox = navigator.userAgent.indexOf('Firefox') > -1;
var is_safari = navigator.userAgent.indexOf("Safari") > -1;
var is_Opera = navigator.userAgent.indexOf("Presto") > -1;
if ((is_chrome)&&(is_safari)) {is_safari=false;}

var DependenciesParameters = (function() {
  return {
	nextID: 0
  }
})()

var DependenciesDrawing = (function() {

	var getRightMostAnchor = function(anchors) {
		for (var i=anchors.length-1; i >= 0; i--) {
			if (anchors[i] != -1) {
				var v = anchors[i];
				anchors[i] = -1;
				return v;
			}
		}
	};

	var getLeftMostAnchor = function(anchors) {
		for (var i=0; i < anchors.length; i++) {
			if (anchors[i] != -1) {
				var v = anchors[i];
				anchors[i] = -1;
				return v;
			}
		}
	};

	var getTokenLeftsAndWidths = function(element, tokenOffsets, documentOffset) {
		if (!documentOffset) documentOffset = 0

		var FROM = 0
		var TO = 1

		// create a hidden element to keep entire article without wrap, then measure token dims and offsets
		var hidden, hiddenContainer;
		hiddenContainer = goog.dom.createDom('div',
				{'style':'position:absolute;width:0px;height:0px;overflow:hidden', 'class':'serif'} ,[
			hidden = goog.dom.createDom('div',
				{'style':'visibility:visible;white-space:nowrap;word-spacing:0px'})//
		]);
		var text = goog.dom.getRawTextContent(element[0]);
		var outers = new Array(), inners = new Array();

		for (var j=0, jj = tokenOffsets.length; j < jj; j++) {
			// token has offsets t.f, t.t
			var t = tokenOffsets[j];
			var outerEnd = (j < jj - 1)? tokenOffsets[j+1][FROM] - documentOffset: t[TO] - documentOffset;

			var tokenText = text.substring(t[FROM] - documentOffset, t[TO] - documentOffset);
			var whitespaceText = text.substring(t[TO] - documentOffset, outerEnd);
			var inner, outer;
			outer = goog.dom.createDom('span', {}, [
			      inner = goog.dom.createDom('span', {}, [
			           goog.dom.createTextNode(tokenText)
			      ]),
			      document.createTextNode(whitespaceText)
			]);
			hidden.appendChild(outer);
			outers.push(outer);
			inners.push(inner);
		}
		//document.body.appendChild(hiddenContainer);
		element.parent()[0].appendChild(hiddenContainer);

		var tokenLefts = new Array, tokenWidths = new Array();
        if (is_safari) {

            $.each(inners, function(i, value) {
                // note: we used to have
                //tokenLefts.push(value.offsetLeft);
                // here, but this gives pixel-precision, whereas the following gives subpixel-precision
                tokenLefts.push(value.getBoundingClientRect().left)
            });
            $.each(inners, function(i, value) {
                //tokenWidths.push(value.offsetWidth);
                tokenWidths.push(value.getBoundingClientRect().width)
            });
        } else {
            $.each(inners, function(i, value) {
                // note: we used to have
                tokenLefts.push(value.offsetLeft);
                // here, but this gives pixel-precision, whereas the following gives subpixel-precision
                //tokenLefts.push(value.getBoundingClientRect().left)
            });
            $.each(inners, function(i, value) {
                tokenWidths.push(value.offsetWidth);
                //tokenWidths.push(value.getBoundingClientRect().width)
            });
        }


//		goog.dom.removeNode(hiddenContainer);

		return [ tokenLefts, tokenWidths];
	};

	var createDrawing = function(name, deps, tokenLefts, tokenWidths) {

		/*   deps: [ {from:12,to:14}, {...}, ] */

		// convert to device pixels (for Retina displays)
		var devicePixelRatio = window.devicePixelRatio;
		var deviceTokenLefts = [], deviceTokenWidths = [];
		$.each(tokenLefts, function(i, value)
				{ deviceTokenLefts.push(value*devicePixelRatio); });
		$.each(tokenWidths, function(i, value)
				{ deviceTokenWidths.push(value*devicePixelRatio); });

		// not sure if this cast is necessary
		for (var i=0; i < deps.length; i++) {
			deps[i].from = parseInt(deps[i].from);
			deps[i].to = parseInt(deps[i].to);
		}

		// remove root
		var rootPos = -1;
		for (var i=0; i < deps.length; i++) {
			if (deps[i].from < 0 || deps[i].to < 0) rootPos = i;
		}
		if (rootPos >= 0) goog.array.removeAt(deps, rootPos);

		// determine anchor points on labels
		var len = deviceTokenWidths.length;
		var numAnchors = new Array(len);
		for (var i=0; i < len; i++) numAnchors[i] = 0;
		for (var i=0; i < deps.length; i++) {
			numAnchors[deps[i].from]++; numAnchors[deps[i].to]++; }
		var anchorXs = new Array(len);
		for (var i=0; i < len; i++) {
			var gap = deviceTokenWidths[i] / (numAnchors[i]+1);
			var ax = anchorXs[i] = new Array(numAnchors[i]);
			for (var j=0; j < numAnchors[i]; j++)
				ax[j] = deviceTokenLefts[i] + (1+j)*gap;
		}

		// sort deps by length
		goog.array.sort(deps, function(a,b) {
		    var l1 = Math.abs(a.to - a.from);
		    var l2 = Math.abs(b.to - b.from);
		    return l1 - l2;
		});

		var highestLevels = new Array(len-1);
		for (var i=0; i < highestLevels.length; i++) highestLevels[i] = 0;

		// for each dep, compute level
		for (var i=0; i < deps.length; i++) {
		    var d = deps[i];
		    var l = (d.from < d.to)? d.from : d.to;
		    var r = (d.from > d.to)? d.from : d.to;
		    var max = 0;
		    for (var j=l; j < r; j++)
		       max = Math.max(highestLevels[j],max);
		    d.level = (max+1);
		    for (var j=l; j < r; j++)
		       highestLevels[j] = (max+1);
		}

		// determine size of deps canvas
		var LEVEL_HEIGHT = 15*devicePixelRatio; //30;
		var max = 0;
		for (var i=0; i < highestLevels.length; i++)
		     max = Math.max(highestLevels[i], max);
		//var max_x = 0;
		//for (var i=0; i < x.length; i++)
		//     max_x = Math.max(x[i], max_x);
		var max_x = deviceTokenLefts[len-1] + deviceTokenWidths[len-1];
		var max_y = max*LEVEL_HEIGHT + 20;

		// invert levels (largest number is bottom, so we can later
		// just multiply with LEVEL_HEIGHT)
		for (var i=0; i < deps.length; i++) {
		    var d = deps[i];
		    d.level = max - d.level + 1;
		}

		// there's a hard limit on the dimensions of a canvas
		// (in Chrome, its 32,767 pixels)

		max_x = Math.min(32767, max_x);
		max_y = Math.min(32767, max_y);

		var ctx, canvas;
		if (document.getCSSCanvasContext) {
			ctx = document.getCSSCanvasContext("2d", name, max_x, max_y);
		} else {
			canvas = angular.element('<canvas id="' + name +'" width="' + max_x + '" height="' + max_y + '" style="display:none"></canvas>');
			document.body.appendChild(canvas[0]);
			//$('document').append(el);
			//ctx = document.append($('canvas'))
			ctx = canvas[0].getContext('2d');
		}


		//var ctx_highlight = document.getCSSCanvasContext("2d", name, max_x, max_y);

		// compute arc connection points
		var style = 'rgb(200,200,200)';
		for (var i=0; i < deps.length; i++) {
		    var d = deps[i];

		    //if (d.from < 0) continue; // ignore root dep

		    ctx.lineWidth = devicePixelRatio; //2;
		    ctx.lineJoin = 'miter';
		    ctx.strokeStyle=style;
		    ctx.beginPath();
		    var xto, xfrom;
		    // due to sentence length restriction, can't display all arcs
		    if (d.to >= anchorXs.length || d.from >= anchorXs.length)
		    	continue;
		    if (d.to > d.from) {
		    	xfrom = getRightMostAnchor(anchorXs[d.from]);
		    	xto = getLeftMostAnchor(anchorXs[d.to]);
			} else {
		    	xfrom = getLeftMostAnchor(anchorXs[d.from]);
		    	xto = getRightMostAnchor(anchorXs[d.to]);
			}
//		    if (xfrom >= 32767) {
//		    	console.log('POINT OUTSIDE ctx')
//		    	continue
//		    }
		    ctx.moveTo(xfrom, max_y);
		    ctx.lineTo(xfrom, d.level*LEVEL_HEIGHT+20);

		    if (Math.abs(xfrom - xto) < 40) {
		    	// the points are too close, draw one arc
		    	if (d.to > d.from)
		    		ctx.arc( xfrom + (xto - xfrom)/2, d.level*LEVEL_HEIGHT+20, (xto - xfrom)/2, Math.PI, 0, false);
		    	else if (d.to < d.from)
		    		ctx.arc( xto + (xfrom - xto)/2, d.level*LEVEL_HEIGHT+20, (xfrom - xto)/2, 0, Math.PI, true);
		    	else {
		    		//d.to == d.from
		    		console.log('WARNING: cannot handle special case where dependency goes from one token to itself')
		    	}
		    } else {
		    	// the points are further apart, draw two arcs connected by a straight line

			    if (d.to > d.from) {
			    	ctx.arc( xfrom+20, d.level*LEVEL_HEIGHT+20, 20, Math.PI, Math.PI*3/2, false);
			    	ctx.lineTo(xto-20, d.level*LEVEL_HEIGHT);
			    	ctx.arc( xto-20, d.level*LEVEL_HEIGHT+20, 20, Math.PI*3/2, 0, false);
				} else {
			    	ctx.arc( xfrom-20, d.level*LEVEL_HEIGHT+20, 20, 0, Math.PI*3/2, true);
			    	ctx.lineTo(xto+20, d.level*LEVEL_HEIGHT);
			    	ctx.arc( xto+20, d.level*LEVEL_HEIGHT+20, 20, Math.PI*3/2, Math.PI, true);
				}
		    }
		    ctx.lineTo(xto, max_y-2);

		    // draw arrow
		    ctx.moveTo(xto-3, max_y-5);
		    ctx.lineTo(xto, max_y-2);
		    ctx.lineTo(xto+3, max_y-5);
		    ctx.stroke();

		    // draw label
		    ctx.font = (devicePixelRatio*7) + 'pt Arial';
		    //ctx.font = '14pt Arial';
		    ctx.textAlign = 'center';
		    ctx.fillText(d.name, (xfrom+xto)/2, d.level*LEVEL_HEIGHT-4*devicePixelRatio);
		}


		//if (canvas) {
		//	var image = new Image();
		//	image.src = canvas.toDataURL("image/png");
		//}
//		return image;

//		var el = angular.element('<canvas id="' + name +'" width="' + max_x + '" height="' + max_y + ' style="display:none"></canvas>');
//		document.body.appendChild(el[0]);
//		console.log(document.body);


		return {
//			image:
			canvas:canvas,
			ctx:ctx,
			deviceWidth: max_x,
			deviceHeight: max_y,
			width: (max_x / devicePixelRatio),
			height: (max_y / devicePixelRatio),
			highestLevels: highestLevels,
			deviceTokenLefts: deviceTokenLefts,
			deviceTokenWidths: deviceTokenWidths
		};
	};

	return {
		getTokenLeftsAndWidths: getTokenLeftsAndWidths,
		createDrawing: createDrawing
	};
})()



var EdgesVisualization = function(element, tokenOffsets, sentenceOffsets, sentenceTokenOffsets, sentenceDependencies) {

	var state = {
	 	renderedSpans: new Array(),
	 	drawings:[],
	 	names:[],
	 	namePrefix:"prefix",
	 	destroyed: false
	};

	var createWithAnnotationsSentence = function(sentNum, state, element, renderedSpans, tokenOffsets, dependencies, tokenLefts, tokenWidths) {
		var nextID = DependenciesParameters.nextID
		DependenciesParameters.nextID = DependenciesParameters.nextID + 1
		var name = state.namePrefix + nextID
		state.names.push(name)

		// create canvas context
		var drawing = DependenciesDrawing.createDrawing(name,
				dependencies, tokenLefts, tokenWidths)

		state.drawings.push(drawing)

		var height = drawing.height
		var width = drawing.width;

		// refresh tokens with ctx info (show partial canvas for each token)
		$.each(renderedSpans, function(i, rs) {

			var firstSpan = rs.sels[0];
			var left = tokenLefts[i] - tokenLefts[0];

			var tokenWidth = (i < tokenLefts.length-1)? tokenLefts[i+1] - tokenLefts[i] : tokenWidths[i];

			if (is_safari) tokenWidth = tokenWidth + 1

			var el = goog.dom.createDom('div', { 'style' :
					'position:absolute;' +
					'top:-' + height + 'px;' +
					'left:0px;right:0px;' +
					//'z-index:-10;' +
					'z-index:0;' +
					'background:-webkit-canvas(' + name + ') no-repeat -' + left + 'px 0px;' +
					'background:-moz-element(#' + name + ') no-repeat -' + left + 'px 0px;' +
//					'background:' + scope.drawing.canvas[0].toDataURL("image/png") + ' no-repeat + -' + left + 'px 0px;' +
//					'background:-moz-element(#jojo) no-repeat -' + left + 'px 0px;' +
					'background-size:' + width + 'px;' +
					'width:' + tokenWidth + 'px;' +
					'height:' + height + 'px'
			});
			// if you want all lines to be equal height, set marginTop as follows
			// var marginTop = height;
			var marginTop = (drawing.highestLevels[i]+1) * 15;
			// if you want to use inline rather than inline-block spans, use following line
			//$(firstSpan).attr('style', 'display:inline;line-height:' + (marginTop + 20) +
			//   'px;margin-top:' + marginTop + 'px;position:relative');
			$(firstSpan).attr('style', 'display:inline-block;margin-top:' + marginTop + 'px;position:relative')
			firstSpan.appendChild(el)
			rs.aux = new Array()
			rs.aux.push(el)
		}) //, this
	};

	var FROM = 0
	var TO = 1

	var createWithAnnotations = function(element, state, renderedSpans, tokenOffsets, sentenceOffsets, sentenceTokenOffsets, dependencies) {

		var documentOffset = 0 //scope.document.offset
		var documentTokenOffset = 0 //scope.document.tokenOffset

		var r = DependenciesDrawing.getTokenLeftsAndWidths($(element), tokenOffsets, documentOffset)
		var tokenLefts = r[0], tokenWidths = r[1];

		// insert spans
		CharOffsets.createMultiRangeSpans([element,this], tokenOffsets, renderedSpans, documentOffset)

		//var maxSentences = Math.min(25, sentenceTokenOffsets.length)
		var maxSentences = sentenceTokenOffsets.length
		//console.log('sentences ' + sentenceTokenOffsets.length)
		for (var sentNum = 0; sentNum < maxSentences; sentNum++) {
			var sto = sentenceTokenOffsets[sentNum]
			var lsto = [sto[FROM] - documentTokenOffset, sto[TO] - documentTokenOffset]
			if (lsto[FROM] >= lsto[TO]) continue
			var toks = tokenOffsets.slice(lsto[FROM], lsto[TO])
			var deps = dependencies[sentNum]
			var sntTokenLefts = tokenLefts.slice(lsto[FROM], lsto[TO])
			var shift = sntTokenLefts[0]
			for (var i=0; i < sntTokenLefts.length; i++)
				sntTokenLefts[i] = sntTokenLefts[i] - shift
			var sntTokenWidths = tokenWidths.slice(lsto[FROM], lsto[TO])
			var sntRenderedSpans = renderedSpans.slice(lsto[FROM], lsto[TO])
			createWithAnnotationsSentence(sentNum, state, element, sntRenderedSpans, toks, deps, sntTokenLefts, sntTokenWidths)
		}

    }


	// nothing to do
	if (tokenOffsets.length == 0) return

	//element.hide() // avoid reflows

	var sentenceDependencies = [[{"from":0, "to":1, "name":"dep"},{"from":2, "to":1, "name":"dep"}]]
	createWithAnnotations(element, state, state.renderedSpans, tokenOffsets, sentenceOffsets, sentenceTokenOffsets, sentenceDependencies)
	//element.show()





	state.destroy = function() {
		if (state.destroyed) return;
		state.destroyed = true;
		$.each(state.renderedSpans, function(i, value) {
			// do bound listeners automatically get destroyed??
            //value.element.remove();
            //value.scope.$destroy();

			$.each(value.aux, function(j,n) {
				goog.dom.removeNode(n);
			});
			$.each(value.sels, function(j,n) {
				goog.dom.flattenElement(n);
			});
			value.sels = [];

		});
		goog.editor.range.normalizeNode(element);
		state.renderedSpans.length = 0; // clear array

		// need to destroy 2d context,
		// the following works, but is slow
		$.each(state.drawings, function(i, drawing) {
			var ctx = drawing.ctx;
			ctx.clearRect(0,0,drawing.deviceWidth,
					drawing.deviceHeight);
		});
		state.drawings = []
		state.names = []

		//element.remove();
		//goog.editor.range.normalizeNode(element[0]);
		//state.renderedSpans.length = 0;
	}
	return state

}

module.exports = EdgesVisualization
},{"./CharOffsets.js":5}],7:[function(require,module,exports){
/* TokensVisualization */
var CharOffsets = require('./CharOffsets.js')

var Span = function(sels) {
    var state = {}

	var fragment = function(i, length) {
		var fragment = '';
		if (i==0 && i < length-1) fragment = 'left';
		else if (i==0 && i==length-1) fragment = 'leftright';
		else if (i==length-1 && i > 0) fragment = 'right';
		else if (i > 0 && i < length-1) fragment = 'inner';
		return fragment;
	};

    // initialize
    state.sels = sels
    state.color = 'red'
	if (!sels) return;
	var ii = sels.length;
	$.each(sels, function(i, sel) {
		$(sel).addClass('highlight_' + state.color);
		$(sel).addClass('highlight_' + fragment(i, ii));
		//$(sel).on('click', function() {
		//	console.log('clicked');
		//});
    })

    state.destroy = function() {
    	// unbind all handlers
		if (!state.sels) return;
				
		$.each(state.sels, function(sel) {
			//$(sel).unbind('click');
		});
    }
    return state
}

var SpansVisualization = function(element, spans) {
	var state = {
	 	renderedSpans: new Array(),
	 	destroyed: false
	};

	//var documentOffset = scope.document.offset
	var documentOffset = 0
		
	CharOffsets.createMultiRangeSpans([element,this], spans, state.renderedSpans, documentOffset)

	$.each(state.renderedSpans, function(i, rs) {
		var span = new Span(rs.sels)
	});

	state.destroy = function() {
		state.destroyed = true;
		$.each(state.renderedSpans, function(i, value) {
			// do bound listeners automatically get destroyed??
            //value.element.remove();
            //value.scope.$destroy();

			//$.each(value.aux, function(j,n) {
			//	goog.dom.removeNode(n);
			//});
			$.each(value.sels, function(j,n) {
				goog.dom.flattenElement(n);
			});
			value.sels = [];			
		});
		//element.remove();
		//goog.editor.range.normalizeNode(element[0]);
		state.renderedSpans.length = 0;     
	}
	return state
}

module.exports = SpansVisualization
},{"./CharOffsets.js":5}],8:[function(require,module,exports){
/* TokenTagsVisualization */

var CharOffsets = require('./CharOffsets.js')

var TokenTagsVisualization = function(element, tokenOffsets, tags) {
	var state = {
	 	renderedSpans: new Array(),
	 	destroyed: false
	};

	//var documentOffset = scope.document.offset
	var documentOffset = 0

	// insert spans
	CharOffsets.createMultiRangeSpans([element,this], tokenOffsets, state.renderedSpans, documentOffset)

	$.each(state.renderedSpans, function(i, rs) {
		var firstSpan = rs.sels[0]
		var el = goog.dom.createDom('div', { 'style' :
			'position:absolute;' +
			'top:-15px;' +
			'left:0px;right:0px;' +
			'z-index:0;' +
			'width:100px;' + //' + tokenWidth + 'px;' +
			'height:20px;' +
			'color:red;' +
			'font-size:10px;' +
			'font-family:helvetica,arial;' +
			'font-stretch:semi-condensed;' +
			'font-weight:500;'/* +
			'background-color:white'*/
		})
		el.appendChild(goog.dom.createTextNode(tags[i]))
		// if you want all lines to be equal height, set marginTop as follows
		//var marginTop = (drawing.highestLevels[i]+1) * 15;
			// if you want to use inline rather than inline-block spans, use following line
			//$(firstSpan).attr('style', 'display:inline;line-height:' + (marginTop + 20) +
			//   'px;margin-top:' + marginTop + 'px;position:relative');
		var marginTop = 10
		$(firstSpan).attr('style', 'display:inline-block;margin-top:' + marginTop + 'px;position:relative')
		firstSpan.appendChild(el)
		rs.aux = new Array()
		rs.aux.push(el)
		})

	state.destroy = function() {
		state.destroyed = true;
		$.each(state.renderedSpans, function(i, value) {
			$.each(value.aux, function(j, n) {
				goog.dom.removeNode(n);
			})
			$.each(value.sels, function(j, n) {
				goog.dom.flattenElement(n);
			})
			value.sels = [];
		});
		//element.remove();
		//goog.editor.range.normalizeNode(element[0]);
		state.renderedSpans.length = 0;
	}
	return state
}

module.exports = TokenTagsVisualization

},{"./CharOffsets.js":5}]},{},[2]);
