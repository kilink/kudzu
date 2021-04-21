package com.copperleaf.kudzu.parser.expression

import com.copperleaf.kudzu.node.Node
import com.copperleaf.kudzu.node.choice.ChoiceNode
import com.copperleaf.kudzu.node.expression.PostfixOperatorNode
import com.copperleaf.kudzu.node.many.ManyNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import com.copperleaf.kudzu.parser.ParserResult
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.many.ManyParser
import com.copperleaf.kudzu.parser.mapped.FlatMappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser

/**
 * The parser for a level of combined [Operator.Postfix] operators of the same precedence.
 */
@ExperimentalStdlibApi
@Suppress("UNCHECKED_CAST")
class PostfixOperatorParser(
    private val operator: ExactChoiceParser,
    private val operand: Parser<Node>
) : Parser<PostfixOperatorNode> {

    private val parser: Parser<PostfixOperatorNode> by lazy {
        val impl = SequenceParser(
            operand,
            ManyParser(operator),
        )

        FlatMappedParser(impl) { sequenceNode ->
            val (operandNode, manyOperatorsNode) = sequenceNode.children
            val operatorNodes = (manyOperatorsNode as ManyNode<ChoiceNode>)
                .nodeList
                .map { it.node }

            PostfixOperatorNode(
                operandNode,
                operatorNodes,
                sequenceNode.context
            )
        }
    }

    override fun predict(input: ParserContext): Boolean {
        return parser.predict(input)
    }

    override val parse: DeepRecursiveFunction<ParserContext, ParserResult<PostfixOperatorNode>> = parser.parse
}
