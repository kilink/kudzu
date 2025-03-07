package com.copperleaf.kudzu.parser.tag

import com.copperleaf.kudzu.node.Node
import com.copperleaf.kudzu.node.tag.TagNameNode
import com.copperleaf.kudzu.node.tag.TagNode
import com.copperleaf.kudzu.parser.ParseFunction
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import com.copperleaf.kudzu.parser.ParserException
import com.copperleaf.kudzu.parser.mapped.FlatMappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser

/**
 * A simple, non-necessarily-recursive tag parser. It matches a sequence of "open tag", "content", "close tag".
 */

public class SimpleTagParser<Opening : Node, Content : Node, Closing : Node>(
    public val name: String,
    public val openingParser: Parser<TagNameNode<Opening>>,
    public val contentParser: Parser<Content>,
    public val closingParser: Parser<TagNameNode<Closing>>,
) : Parser<TagNode<Opening, Content, Closing>> {

    private val parser: Parser<TagNode<Opening, Content, Closing>> by lazy {
        FlatMappedParser(
            SequenceParser(
                openingParser,
                contentParser,
                closingParser,
            )
        ) { (nodeContext, openingNode, contentNode, closingNode) ->
            if (openingNode.tagName != closingNode.tagName) {
                throw ParserException(
                    "Mismatched closing tag: Expected tag name to be " +
                        "'${openingNode.tagName}', got '${closingNode.tagName}'",
                    parser = this@SimpleTagParser,
                    input = this
                )
            }

            TagNode(openingNode, contentNode, closingNode, nodeContext)
        }
    }

    override fun predict(input: ParserContext): Boolean = parser.predict(input)

    override val parse: ParseFunction<TagNode<Opening, Content, Closing>> get() = parser.parse
}
